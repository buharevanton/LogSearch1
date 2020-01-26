import com.alee.utils.FileUtils;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.*;


//Интерфейс программы

public class LogInt extends JFrame {
    private JPanel rootPanel;
    private JButton dirChooseButton; // кнопка для выбора директории для поиска
    private JTextField searchContentField;// текстовое поле содержащее текст для поиска
    private JTextField extensoinTextField; // текстовое поле содержащее расширение файлов для поиска
    private JTabbedPane tabbedPane1; // для отображения файлов в разных табах
    private JButton searchButton; // кнопка поиска по выбранной директории
    private JTextArea textArea1;
    private JScrollPane scrollTree;
    private JPanel tabpanel;
    private JScrollPane exScroll;
    private JButton findAllButton;
    private JProgressBar progressBar1;
    private JScrollBar scrollBar2; // служит для навигации по файлу "вперед" "назад"
    private JList list1;// в него добавляются строки из файла которые содержат искомый текст
    private JTextField charsetField; // поле где можно указать кодировку
    private JTextField timeForSearchInTextField;
    private JTextField timeForSearchInTextField1;
    private volatile JTree jTree; // отображает найденные файлы
    private  Path path;
    private Search search;
    private DefaultListModel<String> defaultListModel = new DefaultListModel<>();

    public LogInt()  {

        setContentPane(rootPanel);
        setVisible(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        setBounds(dimension.width / 2 - 650, dimension.height / 2 - 385, dimension.width-300, dimension.height-200);
        setTitle("LogSearcher");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jTree = new JTree();
        searchButton.setEnabled(false);
        searchContentField.setEnabled(false);
        extensoinTextField.setEnabled(false);

        // выбор директории
        dirChooseButton.addActionListener(e -> {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setDialogTitle("Choose directory");
            directoryChooser.setCurrentDirectory(new File("C:\\"));
            directoryChooser.setSelectedFile(new File("C:\\Program Files"));
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.showOpenDialog(rootPanel);
            final File file = directoryChooser.getSelectedFile();
            path = file.toPath();
            dirChooseButton.setIcon ( FileUtils.getFileIcon ( file ) );
            dirChooseButton.setText ( FileUtils.getDisplayFileName ( file ) );
            searchButton.setEnabled(true);
            searchContentField.setEnabled(true);
            extensoinTextField.setEnabled(true);
        });
        //поиск по директории
        searchButton.addActionListener(e -> {
            SearchLogVisitor searchLogVisitor = new SearchLogVisitor();
            searchLogVisitor.setExtension(extensoinTextField.getText());
            searchLogVisitor.setSearchContent(searchContentField.getText());
            searchLogVisitor.setCharset(charsetField.getText());
            searchLogVisitor.setCharsetField(charsetField);
            search = new Search(searchLogVisitor, path); //класс Search наследуется от Thread
            search.setjTextArea(textArea1);
            search.setjTree(jTree);
            search.setjScrollPane(scrollTree);
            search.setTimeField(timeForSearchInTextField);
            search.setEnbl(dirChooseButton,searchButton,extensoinTextField,searchContentField);
            search.start();// запуск потока
            ImageIcon icon = new ImageIcon("img/loader.gif");
            searchButton.setIcon(icon);
            searchButton.setEnabled(false);
            searchContentField.setEnabled(false);
            extensoinTextField.setEnabled(false);
            dirChooseButton.setEnabled(false);
            jTree.removeAll();
            jTree.setEnabled(false);
        });

        // по нажатию на элемент JTree файл откроется в новом табе
        jTree.addTreeSelectionListener(e -> {
            try{
            String s=jTree.getSelectionPath().toString();
            s=s.replace("[","");
            s=s.replace("]","");
            s=s.replace(", ","\\");
            JPanel childPanel = new JPanel();
            MyPane childText = new MyPane(22);
            childText.setPath(search.getPath(s));
            Font font = new Font("",Font.PLAIN,12);
            childText.setFont(font);
            JScrollPane childScroll = new JScrollPane();
            childScroll.setViewportView(childText);
            childScroll.getHorizontalScrollBar().setVisible(true);
            childPanel.setLayout(new CardLayout());
            childPanel.add(childScroll);
            boolean b = true;
            for (int i=0;i<tabbedPane1.getTabCount();i++){
                if (jTree.getLastSelectedPathComponent().toString().equals(tabbedPane1.getTitleAt(i))){
                    tabbedPane1.setSelectedIndex(i);
                    b=false;
                }
            }
            if (b)
            tabbedPane1.addTab(jTree.getLastSelectedPathComponent().toString(),childPanel);
            if (tabbedPane1.getSelectedIndex()!=0){
            scrollBar2.setValue(0);
            scrollBar2.setMaximum(childText.getScrollMax());}
            BigFile bigFile = new BigFile(search.getPath(s),childText,scrollBar2,timeForSearchInTextField); //класс BigFile наследуется от Thread
            bigFile.setCharset(charsetField.getText());
            bigFile.setjTabbedPane(tabbedPane1);
            for (int i=0;i<tabbedPane1.getTabCount();i++){
                if (jTree.getLastSelectedPathComponent().toString().equals(tabbedPane1.getTitleAt(i)))
                    tabbedPane1.setSelectedIndex(i);
            }
            bigFile.start();
            System.gc();
            System.runFinalization();}
            catch (Exception ex1){

            }
        });

        // поиск заданного текста в открытом файле
        findAllButton.addActionListener(e -> {
            if (tabbedPane1.getSelectedIndex()==0){
                JOptionPane.showMessageDialog(tabbedPane1,"File is not chosen");
            }
            else if (searchContentField.getText().equals("")){
                JOptionPane.showMessageDialog(tabbedPane1,"Nothing to look for");
            }
            else {
            JPanel jPanel = (JPanel) tabbedPane1.getSelectedComponent();
            JScrollPane jScrollPane = (JScrollPane) jPanel.getComponent(0);
            JViewport jViewport = (JViewport) jScrollPane.getComponent(0);
            MyPane jTextPane = (MyPane) jViewport.getComponent(0);
            jTextPane.getDlm().removeAllElements();
            list1.setModel(jTextPane.getDlm());
            list1.setFixedCellHeight(17);
            list1.setFixedCellWidth(600);
            SearchText searchText = new SearchText(jTextPane.getPath(),jTextPane,searchContentField.getText(),progressBar1,timeForSearchInTextField1);
            searchText.setCharset(charsetField.getText());
            searchText.start();}
        });


        tabbedPane1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel jPanel = (JPanel) tabbedPane1.getSelectedComponent();
                JScrollPane jScrollPane = (JScrollPane) jPanel.getComponent(0);
                JViewport jViewport = (JViewport) jScrollPane.getComponent(0);
                if (tabbedPane1.getSelectedIndex()!=0){
                MyPane jTextPane = (MyPane) jViewport.getComponent(0);
                scrollBar2.setValue(0);
                scrollBar2.setMaximum(jTextPane.getScrollMax());
                }
            }
        });

        tabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane1.getSelectedIndex()==0){
                    list1.setModel(defaultListModel);
                }
                else
                if (e.getSource() instanceof JTabbedPane){
                    JPanel jPanel = (JPanel) tabbedPane1.getSelectedComponent();
                    JScrollPane jScrollPane = (JScrollPane) jPanel.getComponent(0);
                    JViewport jViewport = (JViewport) jScrollPane.getComponent(0);
                    MyPane jTextPane = (MyPane) jViewport.getComponent(0);
                    scrollBar2.setValue(0);
                    scrollBar2.setMaximum(jTextPane.getScrollMax());
                    list1.setModel(jTextPane.getDlm());
                }
            }
        });


        extensoinTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                searchButton.setEnabled(true);
            }
        });
        searchContentField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                searchButton.setEnabled(true);
            }
        });

        //навигация по файлу
        scrollBar2.addAdjustmentListener(e -> {
                JPanel jPanel = (JPanel) tabbedPane1.getSelectedComponent();
                JScrollPane jScrollPane = (JScrollPane) jPanel.getComponent(0);
                try{
                JViewport jViewport = (JViewport) jScrollPane.getComponent(0);
                MyPane jTextPane = (MyPane) jViewport.getComponent(0);
                BigFile.prtFile(jTextPane.getPath(),jTextPane,scrollBar2);
                }
                catch (Exception er){}
        });

        // list1 содержит строки из файла которые содержат искомый текст
        // отображает выбранный элемент в файле
        list1.addListSelectionListener(e -> {
            if (tabbedPane1.getSelectedIndex()!=0){
            JPanel jPanel = (JPanel) tabbedPane1.getSelectedComponent();
            JScrollPane jScrollPane = (JScrollPane) jPanel.getComponent(0);
            JViewport jViewport = (JViewport) jScrollPane.getComponent(0);
            MyPane jTextPane = (MyPane) jViewport.getComponent(0);
            String str = jTextPane.getDlm().elementAt(list1.getSelectedIndex());
            String[] s = str.split(": ");
            int index =Integer.parseInt(s[0]);
            if (index>10)
            scrollBar2.setValue(index-10);
            else scrollBar2.setValue(index);
            jTextPane.grabFocus();
            jTextPane.select(jTextPane.getText().indexOf(str),jTextPane.getText().indexOf(str)+str.length()+1);
        }});
    }


    class MyPane extends JTextArea {
        private Path path;
        private ArrayList<Long> map = new ArrayList<>();
        volatile private DefaultListModel<String> dlm = new DefaultListModel<>();
        private Integer scrollMax=0;

        void setScrollMax(Integer scrollMax) {
            this.scrollMax = scrollMax;
        }

        Integer getScrollMax() {
            return scrollMax;
        }

        MyPane(int rows) {
            this.setRows(rows);
        }

        DefaultListModel<String> getDlm() {
            return dlm;
        }

        ArrayList<Long> getMap() {
            return map;
        }

        void setMap(ArrayList<Long> map) {
            this.map = map;
        }

        Path getPath() {
            return path;
        }

        void setPath(Path path) {
            this.path = path;
        }
    }


}
