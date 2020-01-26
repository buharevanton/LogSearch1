import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.Objects;


// Класс для поиска файлов и отображения их в JTree
public class Search extends Thread {
    private SearchLogVisitor searchLogVisitor;
    private Path path;
    private JTextArea jTextArea;
    private JTree jTree;
    private JScrollPane jScrollPane;
    private JTextField timeField;
    private JButton button1;
    private JTextField extension;
    private JTextField searchContent;
    private JButton searchButton;

    public void setEnbl(JButton button1, JButton searchButton, JTextField extension, JTextField searchContent) {
        this.button1=button1;
        this.searchButton=searchButton;
        this.extension=extension;
        this.searchContent=searchContent;
    }

    public void setTimeField(JTextField timeField) {
        this.timeField = timeField;
    }

    public void setjScrollPane(JScrollPane jScrollPane) {
        this.jScrollPane = jScrollPane;
    }

    public void setjTree(JTree jTree) {
        this.jTree = jTree;
    }

    public Search (SearchLogVisitor searchLogVisitor, Path path){
        this.searchLogVisitor=searchLogVisitor;
        this.path=path;
    }

    public void setjTextArea(JTextArea jTextArea) {
        this.jTextArea = jTextArea;
    }

    // добавляет в JTree олько те узлы, которые хранятся в списке найденных файлов
    private void treeWrite(File path, DefaultMutableTreeNode node){
        for (File e: Objects.requireNonNull(path.listFiles())){
            if (e.isDirectory()  && searchLogVisitor.getRelDirectores().containsKey(e.toPath())){
                DefaultMutableTreeNode childNode =new DefaultMutableTreeNode(new FileNode(e));
                node.add(childNode);
                treeWrite(e,childNode);
            }
            else if(searchLogVisitor.getFoundFiles().contains(e.toPath())) {
                DefaultMutableTreeNode childNode =new DefaultMutableTreeNode(new FileNode(e));
                node.add(childNode);
            }
        }
    }

    @Override
    public void run() {
        Date date1 = new Date();
        try {
            Files.walkFileTree(path,searchLogVisitor); // проходит все файлы и поддиректории в заданой директории
        }
        catch (AccessDeniedException e) {
            JOptionPane.showMessageDialog(jTextArea,"AccessDenied "+e.getFile());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
        cleanFileTree(jTree);
        }

        catch (Exception e) {
        }
        Date date2 = new Date();
        long l = date2.getTime()-date1.getTime();
        timeField.setText("Time for search in directory: "+l+" ms");
        jTree.setEnabled(true);
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new FileNode(path.toFile()).toString());
        DefaultTreeModel treeModel = new DefaultTreeModel(top);
        jTree.setModel(treeModel);
        treeWrite(path.toFile(), top);
        jScrollPane.setViewportView(jTree);
        for (int i=0;i<jTree.getRowCount();i++)
            jTree.expandRow(i);

        button1.setEnabled(true);
        extension.setEnabled(true);
        searchContent.setEnabled(true);
        searchButton.setIcon(null);
    }

    private void cleanFileTree(JTree jTree){
        jTree.removeAll();
    }

    public Path getPath (String s){
        Path result = Paths.get(".");
        for (Path string :searchLogVisitor.getFoundFiles()){
            if (string.toString().contains(s)){
                     result = string;
            }
        }
        return result;
    }


    public class FileNode {
        private File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }


}
