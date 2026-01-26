package org.example.baseclases;

import java.io.File;
import java.lang.reflect.Type;


enum FileType{

    Text(new String[]{".txt"}),
    Document(new String[]{".doc", ".docx", ".docm", ".dot", ".dotx", ".dotm", ".docb", ".odt", ".rtf", ".txt", ".xml", ".mht", ".html", ".htm", ".xps", ".wps", ".wpd"}),
    Presentation(new String[]{".ppt", ".pptx", ".pptm", ".pps", ".ppsx", ".ppsm", ".pot", ".potx", ".potm", ".odp"}),
    Table(new String[]{".xls", ".xlsx", ".xlsm", ".xlt", ".xltx", ".xltm", ".xlsb", ".xlam", ".xlw", ".xml", ".csv", ".txt", ".prn", ".dif", ".slk", ".ods", ".mht", ".dbf"}),
    Music(new String[]{".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a", ".ape", ".alac", ".aiff", ".opus", ".mid", ".midi", ".amr", ".ra", ".rm", ".dts", ".ac3", ".mp2", ".mp1", ".voc", ".au", ".snd", ".pcm"}),
    Video(new String[]{".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".mpeg", ".mpg", ".m4v", ".3gp", ".ogg", ".ogv", ".ts", ".mts", ".m2ts", ".vob", ".divx", ".xvid", ".rm", ".rmvb", ".asf", ".swf", ".f4v", ".hevc", ".h265", ".h264", ".avchd", ".mxf", ".dv", ".dav"}),
    Archive(new String[]{".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz", ".lz", ".lzma", ".cab", ".arj", ".z", ".lzh", ".ace", ".iso", ".img", ".dmg", ".jar", ".war", ".ear", ".apk", ".deb", ".rpm", ".msi", ".exe", ".sit", ".sitx", ".arc", ".pak", ".vpk", ".zipx", ".001", ".z01"}),
    Application (new String[]{".exe", ".msi", ".bat", ".cmd", ".com", ".scr", ".pif", ".sh", ".bin", ".run", ".app", ".dmg", ".apk", ".jar", ".war", ".ear", ".deb", ".rpm", ".vbs", ".ps1", ".js", ".jse", ".vbe", ".wsf", ".wsh", ".reg", ".inf", ".sys", ".dll", ".ocx", ".cpl", ".drv", ".efi", ".msp", ".mst", ".action", ".workflow", ".scpt", ".command", ".out", ".elf", ".so", ".dylib", ".ko", ".py", ".pyc", ".pyw", ".rb", ".pl", ".php", ".class"}),
    Pdf(new String[]{".pdf"}),
    Image(new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif", ".webp", ".svg", ".ico", ".raw", ".cr2", ".nef", ".arw", ".dng", ".heic", ".heif", ".psd", ".ai", ".eps", ".indd", ".cdr", ".xcf", ".pdn", ".kra", ".ora", ".clip", ".exr", ".hdr", ".jxr", ".j2k", ".jp2", ".jpf", ".jpm", ".jpx", ".apng", ".avif", ".tga", ".pbm", ".pgm", ".ppm", ".pam", ".pnm", ".ras", ".pcx", ".dds", ".xbm", ".xpm", ".wmf", ".emf", ".cur", ".ani", ".ani", ".icns", ".ico"}),
    Other(new String[]{});

    private final String[] extensions;
    FileType(String[] strings) {
        this.extensions=strings;
    }

    public static FileType fromFilename(String filename) {
        if (filename == null) return null;
        String lower = filename.toLowerCase();
        for (FileType type : values()) {
            for(String extension:type.extensions)
            if (lower.endsWith(extension)) {
                return type;
            }
        }
        return FileType.Other;
    }
}

public class ClientFile {

    private String name;
    private FileType filetype;
    private String extension;
    private String fileway;
    long size;
    ClientFile(String fileway){
        File file=new File(fileway);
        name=file.getName();
        filetype= FileType.fromFilename(name);
        extension=name.substring(name.lastIndexOf('.'));
        size=file.length();
    }
    ClientFile(File file){
        name=file.getName();
        filetype= FileType.fromFilename(name);
        extension=name.substring(name.lastIndexOf('.'));
        fileway=file.getAbsolutePath();
        size=file.length();
    }


}
