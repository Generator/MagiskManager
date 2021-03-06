package com.topjohnwu.magisk.asyncs;

import android.content.Context;
import android.os.Build;

import com.topjohnwu.magisk.MagiskManager;
import com.topjohnwu.magisk.utils.WebService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadBusybox extends ParallelTask<Void, Void, Void> {

    private File busybox;

    public DownloadBusybox(Context context, File bb) {
        busybox = bb;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        getShell().su("rm -rf " + busybox.getParentFile());
        busybox.getParentFile().mkdirs();
        try {
            FileOutputStream out  = new FileOutputStream(busybox);
            InputStream in = WebService.request(WebService.GET,
                    Build.SUPPORTED_32_BIT_ABIS[0].contains("x86") ?
                            MagiskManager.BUSYBOX_X86 :
                            MagiskManager.BUSYBOX_ARM,
                    null
            );
            if (in == null) throw new IOException();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();
            getShell().su_raw(
                    "chmod -R 755 " + busybox.getParent(),
                    busybox + " --install -s " + busybox.getParent()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
