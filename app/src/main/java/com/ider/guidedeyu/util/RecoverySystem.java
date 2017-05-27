/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ider.guidedeyu.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * RecoverySystem contains methods for interacting with the Android
 * recovery system (the separate partition that can be used to install
 * system updates, wipe user data, etc.)
 */
public class RecoverySystem {
    private static final String TAG = "RecoverySystem";

    /**
     * Default location of zip file containing public keys (X509
     * certs) authorized to sign OTA updates. 
     */
    private static final File DEFAULT_KEYSTORE =
        new File("/system/etc/security/otacerts.zip");

    /** Send progress to listeners no more often than this (in ms). */
    private static final long PUBLISH_PROGRESS_INTERVAL_MS = 500;

    /** Used to communicate with recovery.  See bootable/recovery/recovery.c. */
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File UPDATE_FLAG_FILE = new File(RECOVERY_DIR, "last_flag"); 
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private static File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static String LAST_PREFIX = "last_";

    // Length limits for reading files.
    private static int LOG_FILE_MAX_LENGTH = 64 * 1024;

    /**
     * Interface definition for a callback to be invoked regularly as
     * verification proceeds.
     */
    public interface ProgressListener {
        /**
         * Called periodically as the verification progresses.
         *
         * @param progress  the approximate percentage of the
         *        verification that has been completed, ranging from 0
         *        to 100 (inclusive).
         */
        public void onProgress(int progress);
    }

    /** @return the set of certs that can be used to sign an OTA package. */
    private static HashSet<Certificate> getTrustedCerts(File keystore)
        throws IOException, GeneralSecurityException {
        HashSet<Certificate> trusted = new HashSet<Certificate>();
        if (keystore == null) {
            keystore = DEFAULT_KEYSTORE;
        }
        ZipFile zip = new ZipFile(keystore);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream is = zip.getInputStream(entry);
                try {
                    trusted.add(cf.generateCertificate(is));
                } finally {
                    is.close();
                }
            }
        } finally {
            zip.close();
        }
        return trusted;
    }


    private static String getVersion(String version){
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(version);
        return m.replaceAll("").trim();
    }
    

    



    public static void installPackage(Context context, File packageFile)
        throws IOException {
        String filename = packageFile.getCanonicalPath();
        Log.w(TAG, "!!! REBOOTING TO INSTALL " + filename + " !!!");
        String arg = "--update_package=" + filename +
            "\n--locale=" + Locale.getDefault().toString();

        writeFlagCommand(filename);
        bootCommand(context, arg);
    }

	/**
     * Reboots the device in order to install the given rkimgae. 
     *
	 * we try to write private command : "update_rkimage"
	 * add by mmk@rock-chips.com
         * @hide
     */
	public static void installRKimage(Context context, String imagePath)
		throws IOException {
		Log.w(TAG, "!!! REBOOTING TO INSTALL rkimage " + imagePath + " !!!");
		String arg = "--update_rkimage=" + imagePath + 
			"\n--locale=" + Locale.getDefault().toString();
			
		writeFlagCommand(imagePath);
		bootCommand(context, arg);
	}
	
	public static String readFlagCommand() {
		if(UPDATE_FLAG_FILE.exists()) {
			char[] buf = new char[128];
			int readCount = 0;;
			try {
				FileReader reader = new FileReader(UPDATE_FLAG_FILE);
				readCount = reader.read(buf, 0, buf.length);
				Log.d(TAG, "readCount = " + readCount + " buf.length = " + buf.length);
			}catch (IOException e) {
				Log.e(TAG, "can not read /cache/recovery/flag!");
			}finally {
				UPDATE_FLAG_FILE.delete();
			}
			
			StringBuilder sBuilder = new StringBuilder();
			for(int i = 0; i < readCount; i++) {
				if(buf[i] == 0) {
					break;
				}
				sBuilder.append(buf[i]);	
			}
			return sBuilder.toString();
		}else {
			return null;
		}
	}
	
	public static void writeFlagCommand(String path) throws IOException{
		RECOVERY_DIR.mkdirs();
		UPDATE_FLAG_FILE.delete();
		FileWriter writer = new FileWriter(UPDATE_FLAG_FILE);
		try {
			writer.write("updating$path=" + path);
		}finally {
			writer.close();
		}
	}


    public static void rebootWipeUserData(Context context) throws IOException {
        final ConditionVariable condition = new ConditionVariable();

        Intent intent = new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION");
        context.sendOrderedBroadcast(intent, android.Manifest.permission.MASTER_CLEAR,
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        condition.open();
                    }
                }, null, 0, null, null);

        // Block until the ordered broadcast has completed.
        condition.block();

        bootCommand(context, "--wipe_data\n--locale=" + Locale.getDefault().toString());
    }

    /**
     * Reboot into the recovery system to wipe the /cache partition.
     * @throws IOException if something goes wrong.
     */
    public static void rebootWipeCache(Context context) throws IOException {
        bootCommand(context, "--wipe_cache\n--locale=" + Locale.getDefault().toString());
    }

    /**
     * Reboot into the recovery system with the supplied argument.
     * @param arg to pass to the recovery utility.
     * @throws IOException if something goes wrong.
     */
    private static void bootCommand(Context context, String arg) throws IOException {
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable
        LOG_FILE.delete();

        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            command.write(arg);
            command.write("\n");
        } finally {
            command.close();
        }

        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");

        throw new IOException("Reboot failed (no permissions?)");
    }




    private void RecoverySystem() { }  // Do not instantiate
}
