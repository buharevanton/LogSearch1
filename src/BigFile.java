import javax.swing.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Класс для индексации файлов и отображения файлов

public class BigFile extends Thread {
    private Path path;
    private LogInt.MyPane myPane;
    private JScrollBar jScrollBar;
    private ArrayList<Long> map;
    private String charset;
    private JTabbedPane jTabbedPane;

    public void setjTabbedPane(JTabbedPane jTabbedPane) {
        this.jTabbedPane = jTabbedPane;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public BigFile( Path path, LogInt.MyPane myPane, JScrollBar jScrollBar,  JTextField jLabel) {
        this.path=path;
        this.myPane=myPane;
        this.jScrollBar=jScrollBar;
        map=myPane.getMap();
    }

    //В методе run файл индексируется по строкам
    @Override
    public void run() {
        try {
            map.add((long) 0);
            map.add((long) 0);
            myPane.setText("Файл индексируется...");
            RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(),"r");
            String str = randomAccessFile.readLine();
            int shift= (int) randomAccessFile.getFilePointer()-str.length();
            Files.lines(path,Charset.forName(charset)).forEach(s->map.add(map.get(map.size()-1)+s.length()+shift));
            jScrollBar.setMinimum(1);
            jScrollBar.setMaximum(map.size());
            myPane.setScrollMax(map.size());
            myPane.setMap(map);
            System.gc();
             }
         catch (Exception e) {
             JOptionPane.showMessageDialog(myPane,"Might be a wrong charset");
             jTabbedPane.remove(jTabbedPane.getSelectedIndex());
            }
        System.gc();
        System.runFinalization();
    }

    // Чтобы открывать большие файлы они читаются не полностью, а небольшими кусочками и отображаются в TextArea
    // При изменении значения ScrollBar читается и отображается новый кусок файла
    // Метод prtFile служит для чтения и отображения текста из файла
    public static void prtFile (Path path1, LogInt.MyPane myPane, JScrollBar jScrollBar){
        try {
            int l = jScrollBar.getValue();
            RandomAccessFile rf = new RandomAccessFile(path1.toFile(),"r");
            rf.seek(myPane.getMap().get(l));
            StringBuilder s= new StringBuilder();
            for (int i=0;i<myPane.getRows()-1;i++) {
                s.append(l + i).append(": ").append(rf.readLine()).append("\n");
            }
            rf.close();
            myPane.setText(s.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
