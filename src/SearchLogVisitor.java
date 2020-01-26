import javax.swing.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SearchLogVisitor extends SimpleFileVisitor<Path> {
    private String searchContent;
    private String extension;
    private List<Path> foundFiles = new ArrayList<>();
    private Map<Path,Boolean> relDirectores =new HashMap<>();
    private String charset;
    private JTabbedPane jTabbedPane;
    private JTextField charsetField;


    public void setCharsetField(JTextField charsetField) {
        this.charsetField = charsetField;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setjTabbedPane(JTabbedPane jTabbedPane) { this.jTabbedPane = jTabbedPane; }

    public void setSearchContent(String searchContent){
        this.searchContent=searchContent ;}

    public void setExtension (String extension){
        this.extension=extension;
    }

    public List<Path> getFoundFiles() {
        return foundFiles;
    }

    public Map<Path, Boolean> getRelDirectores() {
        return relDirectores;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    // проверяет удовлетворяет ли файл критериям поиска
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toString().endsWith(extension)) {
           if (attrs.size() < 50715200) {
                byte[] content = Files.readAllBytes(file);
               String s = new String(content);
                if (s.contains(searchContent)) {
                    foundFiles.add(file); // если файл подходит по критериям добавляем его в список
                    relDirectores.put(file.getParent(), true);// если директория содержит файлы удовлетворяющие поиску помечаем ее true
               }
           } else {
                try {
                    Files.lines(file, Charset.forName(charset)).filter(s -> s.contains(searchContent)).limit(1).forEach(s->{
                        foundFiles.add(file);// если файл подходит по критериям добавляем его в список
                        relDirectores.put(file.getParent(), true);// если директория содержит файлы удовлетворяющие поиску помечаем ее true
                    });
                }
                catch (UnsupportedCharsetException e){
                    JOptionPane.showMessageDialog(jTabbedPane,"Unsupported Charset \n  will be changed to UTF-8");
                    charsetField.setText("UTF-8");
                    return FileVisitResult.CONTINUE;
                }
                catch ( Exception e){
                    JOptionPane.showMessageDialog(jTabbedPane,"Might be a wrong charset \n"+ file.getFileName());
                }
             }
            }
        return FileVisitResult.CONTINUE;
    }
}
