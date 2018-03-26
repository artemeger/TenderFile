package sample.ipfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import io.ipfs.api.IPFS;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import sample.ipfs.events.DaemonEvent;
import sample.ipfs.events.EventManager;

public class IPFSDaemon {

    private IPFS ipfs;
    private OS os;
    private boolean attached = false;
    private Thread thread = null;
    private File bin;
    private String url = "https://dist.ipfs.io/go-ipfs/v0.4.13/go-ipfs_v0.4.13_";
    private File path = new File("bin");
    private EventManager eventman;

    public static enum OS {
        WINDOWS,
        MAC,
        LINUX,
        FREEBSD;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public boolean isAttached() {
        return attached;
    }

    public IPFS getIPFS() {
        return ipfs;
    }

    public Thread getThread() {
        return thread;
    }

    public File getBin() {
        return bin;
    }

    public IPFSDaemon() {
        getOS();
        if(os == null || (os.equals(OS.FREEBSD) && !is64bits())) {
            System.out.println("System not supported");
            System.exit(1);
        }

        eventman = new EventManager();
    }

    public EventManager getEventManager() {
        return eventman;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void start() {
        if(thread != null && thread.isAlive()) thread.interrupt();
        thread = run(new Runnable() {

            Process init;
            Process daemon;

            public void stop() {
                if(daemon.isAlive()) {
                    daemon.destroy();
                    System.out.println("Daemon stopped");
                    eventman.call(new DaemonEvent(DaemonEvent.DaemonEventType.DAEMON_STOPPED));
                }
            }

            @Override
            public void run() {

                Runtime.getRuntime().addShutdownHook(
                        new Thread(() -> stop())
                );

                new File(getPath(), "repo.lock").delete();

                try {

                    Runtime.getRuntime().addShutdownHook(
                            new Thread(() -> stop())
                    );

                    new File(getPath(), "repo.lock").delete();

                    init = process("init");
                    gobble(init);
                    eventman.call(new DaemonEvent(DaemonEvent.DaemonEventType.INIT_STARTED));
                    init.waitFor();
                    eventman.call(new DaemonEvent(DaemonEvent.DaemonEventType.INIT_DONE));

                } catch(InterruptedException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {

                    daemon = process("daemon");
                    gobble(daemon);
                    eventman.call(new DaemonEvent(DaemonEvent.DaemonEventType.DAEMON_STARTED));
                    daemon.waitFor();

                } catch(InterruptedException e) {

                    stop();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getOS() {
        if(SystemUtils.IS_OS_WINDOWS)
            os = OS.WINDOWS;
        if(SystemUtils.IS_OS_LINUX)
            os = OS.LINUX;
        if(SystemUtils.IS_OS_MAC)
            os = OS.MAC;
        if(SystemUtils.IS_OS_FREE_BSD)
            os = OS.FREEBSD;
    }

    public boolean is64bits() {
        return System.getProperty("os.arch").contains("64");
    }

    public void binaries() throws MalformedURLException, IOException  {
        switch(os) {
            case WINDOWS:{
                bin = new File("bin.exe");
                break;
            }
            case MAC: case LINUX: case FREEBSD: {
                bin = new File("bin");
                break;
            }
        }
        if(!bin.exists()){
            download();
            if(os == OS.LINUX || os == OS.FREEBSD){
                Runtime.getRuntime().exec("chmod +x bin");
            }
        }
    }

    public void getFileFromTarGz(String path, File arch, File destination) throws IOException{
        GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(arch));
        try(TarArchiveInputStream tis = new TarArchiveInputStream(gzis)){
            ArchiveEntry te;
            while((te = tis.getNextEntry()) != null) {
                if(!te.getName().equals(path)) continue;
                FileUtils.copyInputStreamToFile(tis, destination); break;
            }
        }
    }

    public void getFileFromZip(String path, File zip, File destination) throws IOException{
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))){
            ZipEntry ze;
            while((ze = zis.getNextEntry()) != null) {
                if(!ze.getName().equals(path)) continue;
                FileUtils.copyInputStreamToFile(zis, destination); break;
            }
        }
    }

    public void download() throws MalformedURLException, IOException{
        File archive;
        String path;
        String url;
        switch(os) {
            case WINDOWS:{
                url = this.url + "windows" + "-" + "amd64" + ".zip";
                break;
            }
            case MAC:{
                url = this.url + "darwin" + "-" + (is64bits()?"amd64":"386") + ".tar.gz";
                break;
            }
            case LINUX:{
                url = this.url + "linux" + "-" + (is64bits()?"amd64":"386") + ".tar.gz";
                break;
            }
            case FREEBSD:{
                url = this.url + "freebsd" + "-" + "amd64" + ".tar.gz";
                break;
            }
            default: return;
        }

        switch(os) {
            case WINDOWS:{
                archive = new File("bin.zip");
                path = "go-ipfs/ipfs.exe";
                break;
            }
            case MAC: case LINUX: case FREEBSD:{
                archive = new File("bin.tar.gz");
                path = "go-ipfs/ipfs";
                break;
            }
            default: return;
        }

        System.out.println("Downloading bin from " + url + " ...");
        FileUtils.copyURLToFile(new URL(url), archive);
        System.out.println("Bin successfully downloaded");

        switch(os) {
            case WINDOWS:{
                archive = new File("bin.zip");
                getFileFromZip(path, archive, bin);
                break;
            }
            case MAC: case LINUX: case FREEBSD:{
                getFileFromTarGz(path, archive, bin);
                break;
            }
            default: return;
        }
        System.out.println("Bin successfully extracted");
        archive.delete();
    }

    public Thread run(Runnable r) {
        Thread t = new Thread(r);
        t.start();
        return t;
    }

    public Thread run(boolean gobble, String... args) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = process(args);
                    if(gobble) gobble(p);
                    p.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
        return t;
    }

    public void gobble(Process p) {
        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
        errorGobbler.start();
        outputGobbler.start();
    }

    public Process process(String... args) throws IOException{
        File bin;
        String[] cmd;

        Process result;
        switch(os) {
            case WINDOWS:{
                bin = getBin();
                cmd = ArrayUtils.insert(0, args, bin.getPath());
                result = Runtime.getRuntime().exec(cmd, new String[] {"IPFS_PATH="+path.getAbsolutePath()});
                break;
            }
            case MAC: case LINUX: case FREEBSD:{
                cmd = ArrayUtils.insert(0, args, path.getAbsolutePath());
                result = Runtime.getRuntime().exec(cmd);
                break;
            }
            default: result = null;
        }
        return result;
    }

    public void attach() {

        while(!attached) {
            try {
                ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
                ipfs.refs.local();
                attached  = true;
            } catch (Exception e) {}
        }
        eventman.call(new DaemonEvent(DaemonEvent.DaemonEventType.ATTACHED));
        System.out.println("Successfully attached");
    }
}
