import javax.swing.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Класс отвечает за поиск заданного текста в выбранном файле

public class SearchText extends Thread {
    private Path path1;
    private LogInt.MyPane myPane;
    private String searchText;
    private JProgressBar jProgressBar;
    private String charset;
    private JTextField jLabel;

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public SearchText(Path path1, LogInt.MyPane myPane, String searchText, JProgressBar jProgressBar, JTextField jLabel){
       this.jProgressBar=jProgressBar;
       this.myPane=myPane;
       this.path1=path1;
       this.searchText=searchText;
       this.jLabel=jLabel;
    }

    @Override
    public void run() {
        Date date1= new Date();
        List<Integer> bytes = new ArrayList<>();
        try {
            jProgressBar.setMinimum(0);
            jProgressBar.setMaximum(myPane.getScrollMax());
            Files.lines(path1, Charset.forName(charset)).peek(s->bytes.add(bytes.size()+1)).peek(s->jProgressBar.setValue(bytes.size())).filter(s->s.contains(searchText)).forEach(s->myPane.getDlm().addElement(bytes.size()+": "+s));
            // если строка содержит искомый текст добавляем ее в JList
        } catch (Exception e) {
            JOptionPane.showMessageDialog(myPane,"File might be not ready");
        }
        jProgressBar.setValue(0);
        Date date2= new Date();
        long l = date2.getTime()-date1.getTime();
        jLabel.setText("Time for search in file: "+l+" ms");
        if (myPane.getDlm().isEmpty()){
            JOptionPane.showMessageDialog(myPane,"Nothing was found");
        }
    }
}
