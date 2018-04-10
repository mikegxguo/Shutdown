package com.mitac.shutdown;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandManager {

    private static String TAG = "Shutdown";
/*
    public static synchronized String run_command(String[] cmd, String workdirectory){
        StringBuffer result = new StringBuffer();
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);

            InputStream in = null;
            if (workdirectory != null) {
                builder.directory(new File(workdirectory));
                builder.redirectErrorStream(true);
                Process process = builder.start();

                in = process.getInputStream();
                byte[] re = new byte[1024];
                while (in.read(re) != -1) {
                    result = result.append(new String(re));
                }
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }
*/


    public static synchronized String run_command(String[] cmd, String workdirectory) {
        StringBuffer result = new StringBuffer();
        try {
            java.lang.Process process = new ProcessBuilder(cmd).start();
            // try-with-resources
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                            process.getInputStream()))) {
                    String line = null;
                    while((line = bufferedReader.readLine()) != null) {
                        Log.d(TAG, line);
                        result = result.append(line);
                    }
            }
            finally {
                process.destroy();
            }
        } catch (java.io.IOException e) {
            Log.e(TAG, "FAIL");
        }
        return result.toString();
    }

}
