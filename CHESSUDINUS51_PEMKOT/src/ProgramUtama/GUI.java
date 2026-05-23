/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ProgramUtama;

//import ProgramUtama.chesslib.*;
import ProgramUtama.Engine.Client;  //Library Client ref: Catatan Penting 13
import ProgramUtama.Engine.TimerSource;  //Library Client ref: Catatan Penting 13
//////////////LIBRARY CHEES LIB (PERGERAKAN CATUR)///////////////
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
/////////////////////////////////////////////////////////////////
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import static java.util.function.Function.identity;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.github.bhlangonijr.chesslib.CastleRight;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
//import com.sun.speech.freetts.Voice;
//import com.sun.speech.freetts.VoiceManager;
//import com.sun.speech.freetts.Voice;
//import com.sun.speech.freetts.VoiceManager;
import java.io.OutputStream;
import static java.lang.Thread.sleep;
import java.nio.charset.StandardCharsets;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
//import javax.lang.*;

/**
 *
 * @author LENOVO
 */
public final class GUI extends javax.swing.JFrame {

    /**
     * Creates new form GUI
     */
    //Data Icon Bidak Catur
    ///////////STRING PERINTAH////////////
    String PEMBUKA = "HELLO, Tentukan Permainanmu";        //String Welcome
    String WAYAHEKOMPUTER = "Giliran Komputer Bermain";    //waktunya komputer
    String WAYAHMU = "Giliranmu Bermain";                  //waktunya pemain jalan
    String ILLEGAL = "Illegal Movement";                   //ilegal notif
    ///////////////
    public Timer timer1 = null;         //timer untuk permainan catur akses ke UCI dan text serial
    public Timer timerPemain = null;    //timer counter pemain
    public Timer timerKomputer = null;  //timer counter komputer
    //////////////////File2 Gambar////////////////////////////////
    ImageIcon WBenteng = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/wR.png")); 
    ImageIcon WKuda = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/wN.png"));
    ImageIcon WMentri = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/wB.png"));
    ImageIcon WSter = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/wQ.png"));
    ImageIcon WRaja = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/wK.png"));
    ImageIcon WPion = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/wP.png"));
    ImageIcon BBenteng = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/bR.png")); 
    ImageIcon BKuda = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/bN.png"));
    ImageIcon BMentri = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/bB.png"));
    ImageIcon BSter = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/bQ.png"));
    ImageIcon BRaja = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/bK.png"));
    ImageIcon BPion = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/bP.png"));
    ImageIcon LUDN = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/logo-udinus.png"));
    ImageIcon STARTICON = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/start.png"));
    ImageIcon STOPICON = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/stop.png"));
    ImageIcon LOADICON = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/load.png"));
    ImageIcon SAVEICON = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/save.png"));
    ImageIcon PREVICON = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/prev.png"));
    ImageIcon PAUSEICON = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/pause.png"));
    ImageIcon ALARMICON = new ImageIcon(getClass().getResource("/ProgramUtama/Gambar/alarm.png"));
    ///////////////////////////////////////////////////////////////
    String STARTPOINT = "START";  //switch mode button start/stop
    String MODE = "START";  //rad MANUAL AUTOMATIC CLASIC
    String STARTFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0";//"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ kq - 0 0";
    String FEN = STARTFEN;        //FEN yang akan selalu update  
    String WAYAHE = "PEMAIN";     //PEMAIN / KOMPUTER
    Board board = new Board();    //membuat board baru
    int SETMENIT =15;             //pewaktu set timer menit (yang paling atas)
    int SETDETIK =0;              //pewaktu set timer detik (yang paling atas)
    int PEMAINMENIT =15;          //pewaktu pemain menit
    int PEMAINDETIK =0;           //pewaktu pemain detik
    int KOMPUTERMENIT =15;        //pewaktu komputer menit
    int KOMPUTERDETIK =0;         //pewaktu komputer detik
    String movesRecord = "";    //Record movement untuk diupdate ke uci reset jika mengulang permainan
    String bestMoving = " ";      //Hasil Chees Engine
    int counterPlay=0;            //Counter Permainan untuk Penataan posisi string tabel
    ////////////Serial
    SerialPort SerialPort1;
    int dataButKonek=0;
    OutputStream outputStream1;
    String DataBuffer = "";
    int StatusCheck = 0;
    String DataMasuk;
    String FENSTARTGAME = STARTFEN;
    int DataPromote = 0;
    int firstPlay = 1;
    
    
    public GUI() throws InterruptedException, ExecutionException, TimeoutException {
        initComponents();
        //////////////////////VOICE////////////////////
        //Voice voice;
        //VoiceManager vm = VoiceManager.getInstance();
        //System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        //voice = vm.getVoice("kevin16");
        //voice.allocate();
        //voice.speak("HELLO GEEEESSS");
        ///////////////////////////////SET TIMER/////////////////////////////////////
        timer1 = new Timer(1000, (ActionEvent e) -> {
            try {
                MainBos();
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        timerPemain = new Timer(1000, (ActionEvent e) -> {
            if(PEMAINDETIK !=0 || PEMAINMENIT !=0){
                PEMAINDETIK--;
                if(PEMAINDETIK < 0){
                    PEMAINDETIK = 59;
                    PEMAINMENIT--;
                }
            }
            WaktuPemain.setText(TampilTimer(PEMAINMENIT,PEMAINDETIK));
            TextTimerTotal.setText(TampilTimer(PEMAINMENIT,PEMAINDETIK));
        });
        timerKomputer = new Timer(1000, (ActionEvent e) -> {
            if(KOMPUTERDETIK !=0 || KOMPUTERMENIT !=0){
                KOMPUTERDETIK--;
                if(KOMPUTERDETIK < 0){
                    KOMPUTERDETIK = 59;
                    KOMPUTERMENIT--;
                }
            }
            WaktuKomputer.setText(TampilTimer(KOMPUTERMENIT,KOMPUTERDETIK));
            //TextTimerTotal.setText(TampilTimer(KOMPUTERMENIT,KOMPUTERDETIK));
        });
        //////////////////////////////////INISIALISASI/////////////////////////////////////////
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/ProgramUtama/Gambar/logo-udinus.png")));
        this.setTitle("ROCA UDINUS V2.0");
        ResizeIconLabel(LUDN.getImage(),LogoUDINUS);
        TabelPermainan.getTableHeader().setFont(new Font("serif",Font.BOLD,25));
        PrintWayahe("AWAL");
        FEN = board.getFen();
        GoPoint(FEN,"putih");
        LabelWarnaPemain.setIcon(WRaja);
        LabelWarnaKomputer.setIcon(BRaja);
        TextManual.setEditable(false);
        ButKirimData.setEnabled(false);
        WaktuPemain.setEditable(false);
        WaktuKomputer.setEditable(false);
        CountPemain.setEditable(false);
        CountKomputer.setEditable(false);
        JtextFEN.setText(FENSTARTGAME);
        ButPromote.setEnabled(false);
        RadSter.setSelected(true);
        RadSter.setEnabled(false);
        RadMentri.setEnabled(false);
        RadKuda.setEnabled(false);
        RadBenteng.setEnabled(false);
        //PanelDebug.setVisible(false);
        //TextDebug.setEditable(false);
        ResizeIconButton(STARTICON.getImage(),ButMulai);
        ResizeIconButton(LOADICON.getImage(),ButLoad);
        ResizeIconButton(SAVEICON.getImage(),ButSave);
        ResizeIconButton(PREVICON.getImage(),ButPrev);
        ResizeIconButton(PAUSEICON.getImage(),ButPause);
        ResizeIconButton(ALARMICON.getImage(),ButSetTime);
        //board.clear();
        //TextKirim.setText(ProgramUtama.Micromax);
        //////////////////////////////////////////
    }
    ////////baca tuli cmd
    public void get_commandline_results(String cmd){

        
    }
    //////Serial Event Listener
    private void Serial_EventBasedReading(SerialPort activePort){
       activePort.addDataListener(new SerialPortDataListener(){
          @Override
          public int getListeningEvents(){return SerialPort.LISTENING_EVENT_DATA_RECEIVED;}

          @Override
          public void serialEvent(SerialPortEvent spe) {
              //DataBuffer = "";
              byte []newData = spe.getReceivedData();
              for(int i=0; i<newData.length; i++){
                  DataBuffer += (char) newData[i];
                  
                  //.substring(DataBuffer.length()-5,DataBuffer.length()));
              }
              //System.out.println(newData.length);
              //DataBuffer = new String(newData, StandardCharsets.UTF_8);
              //DataBuffer = new String(newData, StandardCharsets);
             
              System.out.print(DataBuffer);
             
              TextTerima.setText(DataBuffer);
          }
       });
    }
    
    public void HitungEngine(String Pergerakan) throws InterruptedException, ExecutionException, TimeoutException{
        ///////////try engine///////////////////////////////////////////
        var client = new Client();
        var position = FEN;

        //client.start(getClass().getResource("/ProgramUtama/Engine/stockfish15.exe").getPath());
        client.start("/C:/Users/H10/Documents/Rocaku/stockfish/stockfish-windows-x86-64-avx2.exe");
        client.command("uci", identity(), (s) -> s.startsWith("uciok"), 2000l);
        client.command("position fen "+ FENSTARTGAME +" moves"+Pergerakan, identity(), s -> s.startsWith("readyok"), 2000l);
        String bestMove = client.command(
                "go movetime 3000",
                lines -> lines.stream().filter(s->s.startsWith("bestmove")).findFirst().get(),
                line -> line.startsWith("bestmove"),
                5000l)
                .split(" ")[1];
        bestMoving = bestMove;
        String DataMusuh = bestMoving.toUpperCase();
        char[] ch = DataMusuh.toCharArray();
        String Awal = ""+ch[0]+ch[1];
        String Akhir = ""+ch[2]+ch[3];
        String DataBoard = String.valueOf(board.getPiece(Square.valueOf(Akhir)));
        if("NONE".equals(DataBoard)) DataBoard = "0";
        else DataBoard = "1";
        String DataLukir;
        String DataLukirCekRaja;
        if(RadHitam.isSelected()){
          DataLukir = String.valueOf(board.getCastleRight(Side.WHITE));
          if("NONE".equals(DataLukir)) DataLukir = "0";
          else DataLukir = "1";
          if(DataLukir == "1"){
            DataLukirCekRaja = String.valueOf(board.getPiece(Square.valueOf(Awal)));
            //System.out.println(DataLukirCekRaja);
            if("WHITE_KING".equals(DataLukirCekRaja)) DataLukir = "1";
            else DataLukir = "0";
          }
          TextKirim.setText(bestMoving+" "+DataBoard+" "+DataLukir+" 0");
          /*String DataLukir = String.valueOf(board.(Side.WHITE));
          if("NONE".equals(DataLukir)) DataLukir = "0";
          else DataLukir = "1";*/
        }
        
        else{
          DataLukir = String.valueOf(board.getCastleRight(Side.BLACK));
          if("NONE".equals(DataLukir)) DataLukir = "0";
          else DataLukir = "1";  
          if(DataLukir == "1"){
            DataLukirCekRaja = String.valueOf(board.getPiece(Square.valueOf(Awal)));
            //System.out.println(DataLukirCekRaja);
            if("BLACK_KING".equals(DataLukirCekRaja)) DataLukir = "1";
            else DataLukir = "0";
          }
          TextKirim.setText(bestMoving+" "+DataBoard+" "+DataLukir+" 1");
        }
        
        //TextDebug.append(DataBoard +"\n");
        //TextKirim.setText(bestMoving+" "+DataBoard+" "+DataLukir);
        client.close();
    }
    ////////////////////////////////////////
    private void ResizeIconLabel(Image img, javax.swing.JLabel Label){
        Label.setText("");
        Image imgScale = img.getScaledInstance(Label.getWidth(), Label.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(imgScale);
        Label.setIcon(scaledIcon);
    }
    
    private void ResizeIconButton(Image img, javax.swing.JButton Button){
        Button.setText("");
        Image imgScale = img.getScaledInstance(Button.getWidth()-10, Button.getHeight()-10, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(imgScale);
        Button.setIcon(scaledIcon);
    }
    
    private void MainBos() throws InterruptedException, ExecutionException, TimeoutException, IOException{
        boolean BolehMain = true;
        if("PEMAIN".equals(WAYAHE)){
            ButPrev.setEnabled(true);
            DataMasuk = TextTerima.getText();
            if(RadAutomatic.isSelected() || RadSemi.isSelected()){
              Serial_EventBasedReading(SerialPort1);
            }
            ///////////
            if("".equals(DataMasuk)){
                 //System.out.println(String.valueOf(board.getSpot(2, 4)));
            }
            else if(DataMasuk.length() < 4 && DataMasuk.length() > 5){
                TextTerima.setText("");
                DataBuffer = "";
            }
            else {
                TextTerima.setText("");
                DataBuffer = "";
                ///////////////cek apakah promosi
                String DataPion = DataMasuk.toUpperCase();
                char[] ch = DataPion.toCharArray();
                String Awal=""+ch[0]+ch[1];
                String Akhir=""+ch[2]+ch[3];
                //cek side
                //String SideTime = String.valueOf(board.getSideToMove());
                if(RadHitam.isSelected()){
                    String DataBoardPion = String.valueOf(board.getPiece(Square.valueOf(Awal))); //BLACK_PAWN 
                    if(DataBoardPion.equals("BLACK_PAWN")){
                       if(ch[1] == '2' && ch[3] == '1'){
                          ButPromote.setEnabled(true);
                          RadSter.setEnabled(true);
                          RadMentri.setEnabled(true);
                          RadKuda.setEnabled(true);
                          RadBenteng.setEnabled(true);
                          DataPromote = 1;
                          PrintWayahe("PROMOTE");
                          JOptionPane.showMessageDialog(this, "Sebelum Melanjutkan Permainan Pilih Bidak Promosi Dulu!!");
                       }
                    }
                }
                else if(RadPutih.isSelected()){
                    String DataBoardPion = String.valueOf(board.getPiece(Square.valueOf(Awal))); //BLACK_PAWN 
                    if(DataBoardPion.equals("WHITE_PAWN")){
                       if(ch[1] == '7' && ch[3] == '8'){
                           ButPromote.setEnabled(true);
                           RadSter.setEnabled(true);
                           RadMentri.setEnabled(true);
                           RadKuda.setEnabled(true);
                           RadBenteng.setEnabled(true);
                           DataPromote = 1;
                           PrintWayahe("PROMOTE");
                           JOptionPane.showMessageDialog(this, "Sebelum Melanjutkan Permainan Pilih Bidak Promosi Dulu!!"); 
                       }
                    }
                }
                
                if(DataPromote == 0){
                    ////Movement Go////////////////////////////////
                    BolehMain = CekIllegalMovement(DataMasuk);
                    if(BolehMain == false){
                        PrintWayahe("ILLEGAL");
                        TextDebug.append("ILLEGAL MOVEMENT\n");
                        
                    }
                    else{
                        //OKE GANTIAN WAYAHE KOMPUTER
                        board.doMove(DataMasuk);
                        movesRecord = movesRecord+" "+DataMasuk;
                        //System.out.println(DataMasuk);
                        PrintWayahe("KOMPUTER");
                        //TextKirim.setText("BENAR");
                        TextDebug.append("OK --> ");
                        DefaultTableModel model = (DefaultTableModel) TabelPermainan.getModel(); 
                        if(RadPutih.isSelected()){
                            model.addRow(new Object[] {DataMasuk, " "});
                            firstPlay = 0;
                        }
                        else{
                            if(firstPlay == 1) {
                                model.addRow(new Object[] {DataMasuk, " "});
                                firstPlay = 0;
                            }
                            else model.setValueAt(DataMasuk, counterPlay, 0);
                            counterPlay++;
                        }
                        FEN = board.getFen();
                        TextDebug.append(FEN+"\n");
                        JtextFEN.setText(FEN);
                        if(RadPutih.isSelected()) GoPoint(FEN,"putih");
                        else GoPoint(FEN,"hitam"); 
                        timerPemain.stop();
                        timerKomputer.start();
                        ////////////////////////////////
                    }
                    ButPrev.setEnabled(false);
                    System.out.println(movesRecord);
                    ///////////////////////////////////////////////
                }
            }
        }
        else if("KOMPUTER".equals(WAYAHE)){ //KOMPUTER MAEN
            //StatusCheck = 2;
            
            ButPrev.setEnabled(false);
            HitungEngine(movesRecord);
            movesRecord = movesRecord+" "+bestMoving;
            
                        //TextKirim.setText("BENAR");
            TextDebug.append("Komputer Gerak :"+bestMoving);
            //System.out.println("Best Moving:" + bestMoving);
            DefaultTableModel model = (DefaultTableModel) TabelPermainan.getModel();
            if(RadPutih.isSelected()){
                if(firstPlay == 1) {
                    model.addRow(new Object[] {" ", bestMoving});  
                    firstPlay = 0;
                }
                else model.setValueAt(bestMoving, counterPlay, 1);
                counterPlay++;
            }
            else{
                model.addRow(new Object[] {" ", bestMoving}); 
                firstPlay = 0;
            }
   
            board.doMove(bestMoving);
            if(board.isMated() == true){
              StatusCheck = 2;
            }
            /*else if(board.isDraw() == true){
              StatusCheck = 3;
            }*/
            else if(board.isKingAttacked()== true){
              StatusCheck = 1;
            }
            else{
              StatusCheck = 0; 
            }
            FEN = board.getFen();
            TextDebug.append("--> "+FEN+"\n");
            JtextFEN.setText(FEN);
            if(RadPutih.isSelected()) GoPoint(FEN,"putih");
            else GoPoint(FEN,"hitam");
            
            //kirim Serial    
            if(RadAutomatic.isSelected() || RadSemi.isSelected()){
              //Serial_EventBasedReading(SerialPort1);
                outputStream1 = SerialPort1.getOutputStream();
                String dataToSend = TextKirim.getText()+"\n";
                try{
                    outputStream1.write(dataToSend.getBytes());
                }
                catch(IOException e){
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
            if(RadManual.isSelected()){
                PrintWayahe("PEMAIN");
                timerPemain.start();
                timerKomputer.stop();
            }
            else{
                PrintWayahe("GERAK");
            }
            //timerPemain.start();
            //timerKomputer.stop();
            System.out.println(movesRecord);
        }
        else if(WAYAHE.equals("GERAK")){
            DataMasuk = TextTerima.getText();
            if(RadAutomatic.isSelected() || RadSemi.isSelected()){
              Serial_EventBasedReading(SerialPort1);
            }
            ///////////
            //if("".equals(DataMasuk)){
                //System.out.println("NO DATA");
            //}
            if("OK".equals(DataMasuk)){
                TextTerima.setText("");
                DataBuffer = "";
                PrintWayahe("PEMAIN");
                timerPemain.start();
                timerKomputer.stop();
                //System.out.println("OK");
            }
            else {
                TextTerima.setText("");
                DataBuffer = "";
                //System.out.println("BUKAN OK");
            }
        }
        else if(WAYAHE.equals("PROMOTE")){
            if(DataPromote == 0){
                ////Movement Go////////////////////////////////
                    if(RadSter.isSelected()) DataMasuk = DataMasuk+"q";
                    else if(RadMentri.isSelected()) DataMasuk = DataMasuk+"b";
                    else if(RadKuda.isSelected()) DataMasuk = DataMasuk+"n";
                    else if(RadBenteng.isSelected()) DataMasuk = DataMasuk+"r";
                    BolehMain = CekIllegalMovement(DataMasuk);
                    if(BolehMain == false){
                        PrintWayahe("ILLEGAL");
                        TextDebug.append("ILLEGAL MOVEMENT\n");
                    }
                    else{
                        //OKE GANTIAN WAYAHE KOMPUTER
                        board.doMove(DataMasuk);
                        movesRecord = movesRecord+" "+DataMasuk;
                        //System.out.println(DataMasuk);
                        
                        //TextKirim.setText("BENAR");
                        TextDebug.append("OK --> ");
                        DefaultTableModel model = (DefaultTableModel) TabelPermainan.getModel(); 
                        if(RadPutih.isSelected()){
                            model.addRow(new Object[] {DataMasuk, " "});
                            firstPlay = 0;
                        }
                        else{
                            if(firstPlay == 1) {
                                model.addRow(new Object[] {DataMasuk, " "});
                                firstPlay = 0;
                            }
                            else model.setValueAt(DataMasuk, counterPlay, 0);
                            counterPlay++;
                        }
                        FEN = board.getFen();
                        TextDebug.append(FEN+"\n");
                        JtextFEN.setText(FEN);
                        if(RadPutih.isSelected()) GoPoint(FEN,"putih");
                        else GoPoint(FEN,"hitam"); 
                        PrintWayahe("KOMPUTER");
                        timerPemain.stop();
                        timerKomputer.start();
                        ////////////////////////////////
                    }
                    ButPrev.setEnabled(false);
                    ButPromote.setEnabled(false);
                    RadSter.setSelected(true);
                    RadSter.setEnabled(false);
                    RadMentri.setEnabled(false);
                    RadKuda.setEnabled(false);
                    RadBenteng.setEnabled(false);
                    System.out.println(movesRecord);
                    ///////////////////////////////////////////////
                    //System.out.println(board.getBackup());
                    //System.out.println(DataMasuk);
            }
        }
    }
    
    public void PrintWayahe(String Sopo){
        if(null != Sopo) {
            switch (Sopo) {
                case "KOMPUTER" -> {
                    WAYAHE = Sopo;
                    Perintah.setText(WAYAHEKOMPUTER);
                    Kalimat.setBackground(Color.blue);
                    //if(RadManual.isSelected()) ButKirimData.setEnabled(false);
                    
                }
                case "PEMAIN" -> {
                    WAYAHE = Sopo;
                    //Perintah.setText(WAYAHMU);
                    //Kalimat.setBackground(Color.green);
                    //if(RadManual.isSelected()) ButKirimData.setEnabled(true);
                    switch (StatusCheck) {
                        case 1:
                            Perintah.setText(WAYAHMU+ " !CHECK!");
                            Kalimat.setBackground(Color.red);
                            break;
                        case 2:
                            Perintah.setText(WAYAHMU+ " #CHECK MATE#");
                            Kalimat.setBackground(Color.red);
                            break;
                        /*case 3:
                            Perintah.setText(WAYAHMU+ " *DRAW*");
                            Kalimat.setBackground(Color.red);
                            break;  */  
                        default:
                            Perintah.setText(WAYAHMU);
                            Kalimat.setBackground(Color.green);
                            break;
                    }
                }
                case "AWAL" -> {
                    Perintah.setText(PEMBUKA);
                    Kalimat.setBackground(Color.black);
                }
                case "ILLEGAL" ->{
                    Perintah.setText(ILLEGAL);
                    Kalimat.setBackground(Color.red);
                }
                case "GERAK" ->{
                    WAYAHE = Sopo;
                    Perintah.setText(WAYAHEKOMPUTER + " " +bestMoving);
                    Kalimat.setBackground(Color.blue);
                }
                case "PROMOTE" ->{
                    WAYAHE = Sopo;
                    Perintah.setText(WAYAHMU);
                    Kalimat.setBackground(Color.green);
                    //DataPromote = 0;
                }
                default -> {
                }
            }
        }
    }
    
    public boolean CekIllegalMovement(String DataMasuk1){
        //DataMasuk = DataMasuk.toUpperCase();
        //char[] ch = DataMasuk1.toCharArray();
        //String Cek = ""+ch[0]+ch[1]+ch[2]+ch[3];
        TextDebug.append("Pemain Gerak "+DataMasuk1+" : ");
        boolean Hasil=false;
        //String Awal=""+ch[0]+ch[1];
        //String Akhir=""+ch[2]+ch[3];
        //if(!CekIfDigit(ch[0]) && CekIfDigit(ch[1]) && !CekIfDigit(ch[2]) && CekIfDigit(ch[3])){
            List<Move> moves = board.legalMoves();
            //String DataLegal = String.valueOf(moves);
            //System.out.println(DataLegal);
            for (Move s : moves){
                if(String.valueOf(s).equals(DataMasuk1)){
                    Hasil = true;
                    break;
                }
                //System.out.println(Cek);
            }
            //Hasil = board.canMove(Square.valueOf(Awal),Square.valueOf(Akhir));
        //}
        
        /*if(Hasil) { // dobel cek apakah hitam yang digerakkan
            //String SIMPANFEN = board.getFen();;
            board.doMove(new Move(Square.valueOf(Awal.toUpperCase()),Square.valueOf(Akhir.toUpperCase())));
            //FEN = board.getFen();
            //if(FEN == null ? SIMPANFEN == null : FEN.equals(SIMPANFEN)) Hasil=false;
            //board.setBoard(board);
            System.out.println(board.toString()+"\n");
            //BoardToFEN();
        }*/
        return Hasil;
    }
    
    public boolean CekIfDigit(char tes){
        boolean Hasil = false;
        if(tes >= '0' && tes <= '9'){
            Hasil = true;
        }
        return Hasil;
    }
    /////////////////////////ChessBoard////////////////////////////////////////////////////////////////////////
    

    
    //////////////////////////////////
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        Arduino = new javax.swing.JPanel();
        TextKirim = new javax.swing.JTextField();
        jComboCOM = new javax.swing.JComboBox<>();
        TextTerima = new javax.swing.JTextField();
        Koneksikan = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        PapanCatur = new javax.swing.JPanel();
        a8p = new javax.swing.JPanel();
        a8 = new javax.swing.JLabel();
        b8p = new javax.swing.JPanel();
        b8 = new javax.swing.JLabel();
        c8p = new javax.swing.JPanel();
        c8 = new javax.swing.JLabel();
        d8p = new javax.swing.JPanel();
        d8 = new javax.swing.JLabel();
        e8p = new javax.swing.JPanel();
        e8 = new javax.swing.JLabel();
        f8p = new javax.swing.JPanel();
        f8 = new javax.swing.JLabel();
        g8p = new javax.swing.JPanel();
        g8 = new javax.swing.JLabel();
        h8p = new javax.swing.JPanel();
        h8 = new javax.swing.JLabel();
        a7p = new javax.swing.JPanel();
        a7 = new javax.swing.JLabel();
        b7p = new javax.swing.JPanel();
        b7 = new javax.swing.JLabel();
        c7p = new javax.swing.JPanel();
        c7 = new javax.swing.JLabel();
        d7p = new javax.swing.JPanel();
        d7 = new javax.swing.JLabel();
        e7p = new javax.swing.JPanel();
        e7 = new javax.swing.JLabel();
        f7p = new javax.swing.JPanel();
        f7 = new javax.swing.JLabel();
        g7p = new javax.swing.JPanel();
        g7 = new javax.swing.JLabel();
        h7p = new javax.swing.JPanel();
        h7 = new javax.swing.JLabel();
        a6p = new javax.swing.JPanel();
        a6 = new javax.swing.JLabel();
        b6p = new javax.swing.JPanel();
        b6 = new javax.swing.JLabel();
        c6p = new javax.swing.JPanel();
        c6 = new javax.swing.JLabel();
        d6p = new javax.swing.JPanel();
        d6 = new javax.swing.JLabel();
        e6p = new javax.swing.JPanel();
        e6 = new javax.swing.JLabel();
        f6p = new javax.swing.JPanel();
        f6 = new javax.swing.JLabel();
        g6p = new javax.swing.JPanel();
        g6 = new javax.swing.JLabel();
        h6p = new javax.swing.JPanel();
        h6 = new javax.swing.JLabel();
        a5p = new javax.swing.JPanel();
        a5 = new javax.swing.JLabel();
        b5p = new javax.swing.JPanel();
        b5 = new javax.swing.JLabel();
        c5p = new javax.swing.JPanel();
        c5 = new javax.swing.JLabel();
        d5p = new javax.swing.JPanel();
        d5 = new javax.swing.JLabel();
        e5p = new javax.swing.JPanel();
        e5 = new javax.swing.JLabel();
        f5p = new javax.swing.JPanel();
        f5 = new javax.swing.JLabel();
        g5p = new javax.swing.JPanel();
        g5 = new javax.swing.JLabel();
        h5p = new javax.swing.JPanel();
        h5 = new javax.swing.JLabel();
        a4p = new javax.swing.JPanel();
        a4 = new javax.swing.JLabel();
        b4p = new javax.swing.JPanel();
        b4 = new javax.swing.JLabel();
        c4p = new javax.swing.JPanel();
        c4 = new javax.swing.JLabel();
        d4p = new javax.swing.JPanel();
        d4 = new javax.swing.JLabel();
        e4p = new javax.swing.JPanel();
        e4 = new javax.swing.JLabel();
        f4p = new javax.swing.JPanel();
        f4 = new javax.swing.JLabel();
        g4p = new javax.swing.JPanel();
        g4 = new javax.swing.JLabel();
        h4p = new javax.swing.JPanel();
        h4 = new javax.swing.JLabel();
        a3p = new javax.swing.JPanel();
        a3 = new javax.swing.JLabel();
        b3p = new javax.swing.JPanel();
        b3 = new javax.swing.JLabel();
        c3p = new javax.swing.JPanel();
        c3 = new javax.swing.JLabel();
        d3p = new javax.swing.JPanel();
        d3 = new javax.swing.JLabel();
        e3p = new javax.swing.JPanel();
        e3 = new javax.swing.JLabel();
        f3p = new javax.swing.JPanel();
        f3 = new javax.swing.JLabel();
        g3p = new javax.swing.JPanel();
        g3 = new javax.swing.JLabel();
        h3p = new javax.swing.JPanel();
        h3 = new javax.swing.JLabel();
        a2p = new javax.swing.JPanel();
        a2 = new javax.swing.JLabel();
        b2p = new javax.swing.JPanel();
        b2 = new javax.swing.JLabel();
        c2p = new javax.swing.JPanel();
        c2 = new javax.swing.JLabel();
        d2p = new javax.swing.JPanel();
        d2 = new javax.swing.JLabel();
        e2p = new javax.swing.JPanel();
        e2 = new javax.swing.JLabel();
        f2p = new javax.swing.JPanel();
        f2 = new javax.swing.JLabel();
        g2p = new javax.swing.JPanel();
        g2 = new javax.swing.JLabel();
        h2p = new javax.swing.JPanel();
        h2 = new javax.swing.JLabel();
        a1p = new javax.swing.JPanel();
        a1 = new javax.swing.JLabel();
        b1p = new javax.swing.JPanel();
        b1 = new javax.swing.JLabel();
        c1p = new javax.swing.JPanel();
        c1 = new javax.swing.JLabel();
        d1p = new javax.swing.JPanel();
        d1 = new javax.swing.JLabel();
        e1p = new javax.swing.JPanel();
        e1 = new javax.swing.JLabel();
        f1p = new javax.swing.JPanel();
        f1 = new javax.swing.JLabel();
        g1p = new javax.swing.JPanel();
        g1 = new javax.swing.JLabel();
        h1p = new javax.swing.JPanel();
        h1 = new javax.swing.JLabel();
        LogoUDINUS = new javax.swing.JLabel();
        Kalimat = new javax.swing.JPanel();
        Perintah = new javax.swing.JLabel();
        Permainan = new javax.swing.JPanel();
        PanPilihWarna = new javax.swing.JPanel();
        RadHitam = new javax.swing.JRadioButton();
        RadPutih = new javax.swing.JRadioButton();
        PanManualMode = new javax.swing.JPanel();
        TextManual = new javax.swing.JTextField();
        ButKirimData = new javax.swing.JButton();
        PanPilihMode = new javax.swing.JPanel();
        RadManual = new javax.swing.JRadioButton();
        RadAutomatic = new javax.swing.JRadioButton();
        RadClassic = new javax.swing.JRadioButton();
        RadSemi = new javax.swing.JRadioButton();
        PanPilihEngine = new javax.swing.JPanel();
        ComboEngine = new javax.swing.JComboBox<>();
        ButTambahkanEngine = new javax.swing.JButton();
        PanPilihLevel = new javax.swing.JPanel();
        ComboLevel = new javax.swing.JComboBox<>();
        ButTambahkanLevel = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        ButLoad = new javax.swing.JButton();
        ButPrev = new javax.swing.JButton();
        ButSave = new javax.swing.JButton();
        ButPause = new javax.swing.JButton();
        ButMulai = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        TabelPermainan = new javax.swing.JTable();
        WaktuPemain = new javax.swing.JTextField();
        WaktuKomputer = new javax.swing.JTextField();
        CountKomputer = new javax.swing.JTextField();
        CountPemain = new javax.swing.JTextField();
        LabelWarnaPemain = new javax.swing.JLabel();
        LabelWarnaKomputer = new javax.swing.JLabel();
        PanelDebug = new javax.swing.JScrollPane();
        TextDebug = new javax.swing.JTextArea();
        TextTimerTotal = new javax.swing.JTextField();
        ButSetTime = new javax.swing.JButton();
        JtextFEN = new javax.swing.JTextField();
        jFENUpdate = new javax.swing.JButton();
        jStartFEN = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        ButPromote = new javax.swing.JButton();
        RadBenteng = new javax.swing.JRadioButton();
        RadSter = new javax.swing.JRadioButton();
        RadMentri = new javax.swing.JRadioButton();
        RadKuda = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setMaximumSize(new java.awt.Dimension(1920, 1080));
        setMinimumSize(new java.awt.Dimension(1920, 1080));
        setPreferredSize(new java.awt.Dimension(1920, 1080));
        setResizable(false);

        Arduino.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Koneksi Arduino", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N
        Arduino.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        TextKirim.setEditable(false);
        TextKirim.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        TextKirim.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                TextKirimInputMethodTextChanged(evt);
            }
        });

        jComboCOM.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jComboCOM.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboCOM.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jComboCOMPopupMenuWillBecomeVisible(evt);
            }
        });

        TextTerima.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        TextTerima.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                TextTerimaInputMethodTextChanged(evt);
            }
        });
        TextTerima.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextTerimaActionPerformed(evt);
            }
        });
        TextTerima.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                TextTerimaPropertyChange(evt);
            }
        });
        TextTerima.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TextTerimaKeyReleased(evt);
            }
        });
        TextTerima.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                TextTerimaVetoableChange(evt);
            }
        });

        Koneksikan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        Koneksikan.setText("Koneksikan");
        Koneksikan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KoneksikanActionPerformed(evt);
            }
        });

        jLabel1.setText("Kirim");

        jLabel4.setText("Terima");

        javax.swing.GroupLayout ArduinoLayout = new javax.swing.GroupLayout(Arduino);
        Arduino.setLayout(ArduinoLayout);
        ArduinoLayout.setHorizontalGroup(
            ArduinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ArduinoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ArduinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboCOM, 0, 119, Short.MAX_VALUE)
                    .addComponent(TextKirim, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ArduinoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ArduinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TextTerima, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Koneksikan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ArduinoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4)))
                .addContainerGap())
        );
        ArduinoLayout.setVerticalGroup(
            ArduinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ArduinoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ArduinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ArduinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TextKirim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TextTerima, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ArduinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboCOM)
                    .addComponent(Koneksikan))
                .addContainerGap())
        );

        PapanCatur.setBackground(new java.awt.Color(0, 0, 0));

        a8p.setBackground(new java.awt.Color(255, 204, 102));
        a8p.setMaximumSize(new java.awt.Dimension(100, 100));
        a8p.setMinimumSize(new java.awt.Dimension(100, 100));
        a8p.setName(""); // NOI18N

        a8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a8.setMaximumSize(new java.awt.Dimension(70, 70));
        a8.setMinimumSize(new java.awt.Dimension(70, 70));
        a8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a8pLayout = new javax.swing.GroupLayout(a8p);
        a8p.setLayout(a8pLayout);
        a8pLayout.setHorizontalGroup(
            a8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        a8pLayout.setVerticalGroup(
            a8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b8p.setBackground(new java.awt.Color(204, 102, 0));

        b8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b8.setMaximumSize(new java.awt.Dimension(70, 70));
        b8.setMinimumSize(new java.awt.Dimension(70, 70));
        b8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b8pLayout = new javax.swing.GroupLayout(b8p);
        b8p.setLayout(b8pLayout);
        b8pLayout.setHorizontalGroup(
            b8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        b8pLayout.setVerticalGroup(
            b8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c8p.setBackground(new java.awt.Color(255, 204, 102));
        c8p.setMaximumSize(new java.awt.Dimension(100, 100));
        c8p.setMinimumSize(new java.awt.Dimension(100, 100));
        c8p.setName(""); // NOI18N

        c8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c8.setMaximumSize(new java.awt.Dimension(70, 70));
        c8.setMinimumSize(new java.awt.Dimension(70, 70));
        c8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c8pLayout = new javax.swing.GroupLayout(c8p);
        c8p.setLayout(c8pLayout);
        c8pLayout.setHorizontalGroup(
            c8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c8pLayout.setVerticalGroup(
            c8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d8p.setBackground(new java.awt.Color(204, 102, 0));

        d8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d8.setMaximumSize(new java.awt.Dimension(70, 70));
        d8.setMinimumSize(new java.awt.Dimension(70, 70));
        d8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d8pLayout = new javax.swing.GroupLayout(d8p);
        d8p.setLayout(d8pLayout);
        d8pLayout.setHorizontalGroup(
            d8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d8pLayout.setVerticalGroup(
            d8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        e8p.setBackground(new java.awt.Color(255, 204, 102));
        e8p.setMaximumSize(new java.awt.Dimension(100, 100));
        e8p.setMinimumSize(new java.awt.Dimension(100, 100));
        e8p.setName(""); // NOI18N

        e8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e8.setMaximumSize(new java.awt.Dimension(70, 70));
        e8.setMinimumSize(new java.awt.Dimension(70, 70));
        e8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e8pLayout = new javax.swing.GroupLayout(e8p);
        e8p.setLayout(e8pLayout);
        e8pLayout.setHorizontalGroup(
            e8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e8pLayout.setVerticalGroup(
            e8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        f8p.setBackground(new java.awt.Color(204, 102, 0));
        f8p.setMaximumSize(new java.awt.Dimension(100, 100));
        f8p.setMinimumSize(new java.awt.Dimension(100, 100));
        f8p.setName(""); // NOI18N

        f8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f8.setMaximumSize(new java.awt.Dimension(70, 70));
        f8.setMinimumSize(new java.awt.Dimension(70, 70));
        f8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f8pLayout = new javax.swing.GroupLayout(f8p);
        f8p.setLayout(f8pLayout);
        f8pLayout.setHorizontalGroup(
            f8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        f8pLayout.setVerticalGroup(
            f8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g8p.setBackground(new java.awt.Color(255, 204, 102));

        g8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g8.setMaximumSize(new java.awt.Dimension(70, 70));
        g8.setMinimumSize(new java.awt.Dimension(70, 70));
        g8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g8pLayout = new javax.swing.GroupLayout(g8p);
        g8p.setLayout(g8pLayout);
        g8pLayout.setHorizontalGroup(
            g8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        g8pLayout.setVerticalGroup(
            g8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        h8p.setBackground(new java.awt.Color(204, 102, 0));

        h8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h8.setMaximumSize(new java.awt.Dimension(70, 70));
        h8.setMinimumSize(new java.awt.Dimension(70, 70));
        h8.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h8pLayout = new javax.swing.GroupLayout(h8p);
        h8p.setLayout(h8pLayout);
        h8pLayout.setHorizontalGroup(
            h8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        h8pLayout.setVerticalGroup(
            h8pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h8pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        a7p.setBackground(new java.awt.Color(204, 102, 0));

        a7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a7.setMaximumSize(new java.awt.Dimension(70, 70));
        a7.setMinimumSize(new java.awt.Dimension(70, 70));
        a7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a7pLayout = new javax.swing.GroupLayout(a7p);
        a7p.setLayout(a7pLayout);
        a7pLayout.setHorizontalGroup(
            a7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        a7pLayout.setVerticalGroup(
            a7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b7p.setBackground(new java.awt.Color(255, 204, 102));

        b7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b7.setMaximumSize(new java.awt.Dimension(70, 70));
        b7.setMinimumSize(new java.awt.Dimension(70, 70));
        b7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b7pLayout = new javax.swing.GroupLayout(b7p);
        b7p.setLayout(b7pLayout);
        b7pLayout.setHorizontalGroup(
            b7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        b7pLayout.setVerticalGroup(
            b7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c7p.setBackground(new java.awt.Color(204, 102, 0));

        c7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c7.setMaximumSize(new java.awt.Dimension(70, 70));
        c7.setMinimumSize(new java.awt.Dimension(70, 70));
        c7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c7pLayout = new javax.swing.GroupLayout(c7p);
        c7p.setLayout(c7pLayout);
        c7pLayout.setHorizontalGroup(
            c7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c7pLayout.setVerticalGroup(
            c7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d7p.setBackground(new java.awt.Color(255, 204, 102));
        d7p.setMaximumSize(new java.awt.Dimension(100, 100));
        d7p.setMinimumSize(new java.awt.Dimension(100, 100));
        d7p.setName(""); // NOI18N

        d7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d7.setMaximumSize(new java.awt.Dimension(70, 70));
        d7.setMinimumSize(new java.awt.Dimension(70, 70));
        d7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d7pLayout = new javax.swing.GroupLayout(d7p);
        d7p.setLayout(d7pLayout);
        d7pLayout.setHorizontalGroup(
            d7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d7pLayout.setVerticalGroup(
            d7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        e7p.setBackground(new java.awt.Color(204, 102, 0));

        e7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e7.setMaximumSize(new java.awt.Dimension(70, 70));
        e7.setMinimumSize(new java.awt.Dimension(70, 70));
        e7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e7pLayout = new javax.swing.GroupLayout(e7p);
        e7p.setLayout(e7pLayout);
        e7pLayout.setHorizontalGroup(
            e7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e7pLayout.setVerticalGroup(
            e7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        f7p.setBackground(new java.awt.Color(255, 204, 102));

        f7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f7.setMaximumSize(new java.awt.Dimension(70, 70));
        f7.setMinimumSize(new java.awt.Dimension(70, 70));
        f7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f7pLayout = new javax.swing.GroupLayout(f7p);
        f7p.setLayout(f7pLayout);
        f7pLayout.setHorizontalGroup(
            f7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        f7pLayout.setVerticalGroup(
            f7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g7p.setBackground(new java.awt.Color(204, 102, 0));

        g7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g7.setMaximumSize(new java.awt.Dimension(70, 70));
        g7.setMinimumSize(new java.awt.Dimension(70, 70));
        g7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g7pLayout = new javax.swing.GroupLayout(g7p);
        g7p.setLayout(g7pLayout);
        g7pLayout.setHorizontalGroup(
            g7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        g7pLayout.setVerticalGroup(
            g7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        h7p.setBackground(new java.awt.Color(255, 204, 102));

        h7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h7.setMaximumSize(new java.awt.Dimension(70, 70));
        h7.setMinimumSize(new java.awt.Dimension(70, 70));
        h7.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h7pLayout = new javax.swing.GroupLayout(h7p);
        h7p.setLayout(h7pLayout);
        h7pLayout.setHorizontalGroup(
            h7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        h7pLayout.setVerticalGroup(
            h7pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h7pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        a6p.setBackground(new java.awt.Color(255, 204, 102));
        a6p.setMaximumSize(new java.awt.Dimension(100, 100));
        a6p.setMinimumSize(new java.awt.Dimension(100, 100));
        a6p.setName(""); // NOI18N

        a6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a6.setMaximumSize(new java.awt.Dimension(70, 70));
        a6.setMinimumSize(new java.awt.Dimension(70, 70));
        a6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a6pLayout = new javax.swing.GroupLayout(a6p);
        a6p.setLayout(a6pLayout);
        a6pLayout.setHorizontalGroup(
            a6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        a6pLayout.setVerticalGroup(
            a6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b6p.setBackground(new java.awt.Color(204, 102, 0));

        b6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b6.setMaximumSize(new java.awt.Dimension(70, 70));
        b6.setMinimumSize(new java.awt.Dimension(70, 70));
        b6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b6pLayout = new javax.swing.GroupLayout(b6p);
        b6p.setLayout(b6pLayout);
        b6pLayout.setHorizontalGroup(
            b6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        b6pLayout.setVerticalGroup(
            b6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c6p.setBackground(new java.awt.Color(255, 204, 102));
        c6p.setMaximumSize(new java.awt.Dimension(100, 100));
        c6p.setMinimumSize(new java.awt.Dimension(100, 100));
        c6p.setName(""); // NOI18N

        c6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c6.setMaximumSize(new java.awt.Dimension(70, 70));
        c6.setMinimumSize(new java.awt.Dimension(70, 70));
        c6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c6pLayout = new javax.swing.GroupLayout(c6p);
        c6p.setLayout(c6pLayout);
        c6pLayout.setHorizontalGroup(
            c6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c6pLayout.setVerticalGroup(
            c6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d6p.setBackground(new java.awt.Color(204, 102, 0));

        d6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d6.setMaximumSize(new java.awt.Dimension(70, 70));
        d6.setMinimumSize(new java.awt.Dimension(70, 70));
        d6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d6pLayout = new javax.swing.GroupLayout(d6p);
        d6p.setLayout(d6pLayout);
        d6pLayout.setHorizontalGroup(
            d6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d6pLayout.setVerticalGroup(
            d6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        e6p.setBackground(new java.awt.Color(255, 204, 102));
        e6p.setMaximumSize(new java.awt.Dimension(100, 100));
        e6p.setMinimumSize(new java.awt.Dimension(100, 100));
        e6p.setName(""); // NOI18N

        e6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e6.setMaximumSize(new java.awt.Dimension(70, 70));
        e6.setMinimumSize(new java.awt.Dimension(70, 70));
        e6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e6pLayout = new javax.swing.GroupLayout(e6p);
        e6p.setLayout(e6pLayout);
        e6pLayout.setHorizontalGroup(
            e6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e6pLayout.setVerticalGroup(
            e6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        f6p.setBackground(new java.awt.Color(204, 102, 0));
        f6p.setMaximumSize(new java.awt.Dimension(100, 100));
        f6p.setMinimumSize(new java.awt.Dimension(100, 100));
        f6p.setName(""); // NOI18N

        f6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f6.setMaximumSize(new java.awt.Dimension(70, 70));
        f6.setMinimumSize(new java.awt.Dimension(70, 70));
        f6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f6pLayout = new javax.swing.GroupLayout(f6p);
        f6p.setLayout(f6pLayout);
        f6pLayout.setHorizontalGroup(
            f6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        f6pLayout.setVerticalGroup(
            f6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g6p.setBackground(new java.awt.Color(255, 204, 102));

        g6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g6.setMaximumSize(new java.awt.Dimension(70, 70));
        g6.setMinimumSize(new java.awt.Dimension(70, 70));
        g6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g6pLayout = new javax.swing.GroupLayout(g6p);
        g6p.setLayout(g6pLayout);
        g6pLayout.setHorizontalGroup(
            g6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        g6pLayout.setVerticalGroup(
            g6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        h6p.setBackground(new java.awt.Color(204, 102, 0));

        h6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h6.setMaximumSize(new java.awt.Dimension(70, 70));
        h6.setMinimumSize(new java.awt.Dimension(70, 70));
        h6.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h6pLayout = new javax.swing.GroupLayout(h6p);
        h6p.setLayout(h6pLayout);
        h6pLayout.setHorizontalGroup(
            h6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        h6pLayout.setVerticalGroup(
            h6pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h6pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        a5p.setBackground(new java.awt.Color(204, 102, 0));

        a5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a5.setMaximumSize(new java.awt.Dimension(70, 70));
        a5.setMinimumSize(new java.awt.Dimension(70, 70));
        a5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a5pLayout = new javax.swing.GroupLayout(a5p);
        a5p.setLayout(a5pLayout);
        a5pLayout.setHorizontalGroup(
            a5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        a5pLayout.setVerticalGroup(
            a5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b5p.setBackground(new java.awt.Color(255, 204, 102));

        b5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b5.setMaximumSize(new java.awt.Dimension(70, 70));
        b5.setMinimumSize(new java.awt.Dimension(70, 70));
        b5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b5pLayout = new javax.swing.GroupLayout(b5p);
        b5p.setLayout(b5pLayout);
        b5pLayout.setHorizontalGroup(
            b5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        b5pLayout.setVerticalGroup(
            b5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c5p.setBackground(new java.awt.Color(204, 102, 0));

        c5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c5.setMaximumSize(new java.awt.Dimension(70, 70));
        c5.setMinimumSize(new java.awt.Dimension(70, 70));
        c5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c5pLayout = new javax.swing.GroupLayout(c5p);
        c5p.setLayout(c5pLayout);
        c5pLayout.setHorizontalGroup(
            c5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c5pLayout.setVerticalGroup(
            c5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d5p.setBackground(new java.awt.Color(255, 204, 102));
        d5p.setMaximumSize(new java.awt.Dimension(100, 100));
        d5p.setMinimumSize(new java.awt.Dimension(100, 100));
        d5p.setName(""); // NOI18N

        d5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d5.setMaximumSize(new java.awt.Dimension(70, 70));
        d5.setMinimumSize(new java.awt.Dimension(70, 70));
        d5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d5pLayout = new javax.swing.GroupLayout(d5p);
        d5p.setLayout(d5pLayout);
        d5pLayout.setHorizontalGroup(
            d5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d5pLayout.setVerticalGroup(
            d5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        e5p.setBackground(new java.awt.Color(204, 102, 0));

        e5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e5.setMaximumSize(new java.awt.Dimension(70, 70));
        e5.setMinimumSize(new java.awt.Dimension(70, 70));
        e5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e5pLayout = new javax.swing.GroupLayout(e5p);
        e5p.setLayout(e5pLayout);
        e5pLayout.setHorizontalGroup(
            e5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e5pLayout.setVerticalGroup(
            e5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        f5p.setBackground(new java.awt.Color(255, 204, 102));

        f5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f5.setMaximumSize(new java.awt.Dimension(70, 70));
        f5.setMinimumSize(new java.awt.Dimension(70, 70));
        f5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f5pLayout = new javax.swing.GroupLayout(f5p);
        f5p.setLayout(f5pLayout);
        f5pLayout.setHorizontalGroup(
            f5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        f5pLayout.setVerticalGroup(
            f5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g5p.setBackground(new java.awt.Color(204, 102, 0));

        g5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g5.setMaximumSize(new java.awt.Dimension(70, 70));
        g5.setMinimumSize(new java.awt.Dimension(70, 70));
        g5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g5pLayout = new javax.swing.GroupLayout(g5p);
        g5p.setLayout(g5pLayout);
        g5pLayout.setHorizontalGroup(
            g5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        g5pLayout.setVerticalGroup(
            g5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        h5p.setBackground(new java.awt.Color(255, 204, 102));

        h5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h5.setMaximumSize(new java.awt.Dimension(70, 70));
        h5.setMinimumSize(new java.awt.Dimension(70, 70));
        h5.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h5pLayout = new javax.swing.GroupLayout(h5p);
        h5p.setLayout(h5pLayout);
        h5pLayout.setHorizontalGroup(
            h5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        h5pLayout.setVerticalGroup(
            h5pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h5pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        a4p.setBackground(new java.awt.Color(255, 204, 102));
        a4p.setMaximumSize(new java.awt.Dimension(100, 100));
        a4p.setMinimumSize(new java.awt.Dimension(100, 100));
        a4p.setName(""); // NOI18N

        a4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a4.setMaximumSize(new java.awt.Dimension(70, 70));
        a4.setMinimumSize(new java.awt.Dimension(70, 70));
        a4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a4pLayout = new javax.swing.GroupLayout(a4p);
        a4p.setLayout(a4pLayout);
        a4pLayout.setHorizontalGroup(
            a4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        a4pLayout.setVerticalGroup(
            a4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b4p.setBackground(new java.awt.Color(204, 102, 0));

        b4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b4.setMaximumSize(new java.awt.Dimension(70, 70));
        b4.setMinimumSize(new java.awt.Dimension(70, 70));
        b4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b4pLayout = new javax.swing.GroupLayout(b4p);
        b4p.setLayout(b4pLayout);
        b4pLayout.setHorizontalGroup(
            b4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        b4pLayout.setVerticalGroup(
            b4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c4p.setBackground(new java.awt.Color(255, 204, 102));
        c4p.setMaximumSize(new java.awt.Dimension(100, 100));
        c4p.setMinimumSize(new java.awt.Dimension(100, 100));
        c4p.setName(""); // NOI18N

        c4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c4.setMaximumSize(new java.awt.Dimension(70, 70));
        c4.setMinimumSize(new java.awt.Dimension(70, 70));
        c4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c4pLayout = new javax.swing.GroupLayout(c4p);
        c4p.setLayout(c4pLayout);
        c4pLayout.setHorizontalGroup(
            c4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c4pLayout.setVerticalGroup(
            c4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d4p.setBackground(new java.awt.Color(204, 102, 0));

        d4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d4.setMaximumSize(new java.awt.Dimension(70, 70));
        d4.setMinimumSize(new java.awt.Dimension(70, 70));
        d4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d4pLayout = new javax.swing.GroupLayout(d4p);
        d4p.setLayout(d4pLayout);
        d4pLayout.setHorizontalGroup(
            d4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d4pLayout.setVerticalGroup(
            d4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        e4p.setBackground(new java.awt.Color(255, 204, 102));
        e4p.setMaximumSize(new java.awt.Dimension(100, 100));
        e4p.setMinimumSize(new java.awt.Dimension(100, 100));
        e4p.setName(""); // NOI18N

        e4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e4.setMaximumSize(new java.awt.Dimension(70, 70));
        e4.setMinimumSize(new java.awt.Dimension(70, 70));
        e4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e4pLayout = new javax.swing.GroupLayout(e4p);
        e4p.setLayout(e4pLayout);
        e4pLayout.setHorizontalGroup(
            e4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e4pLayout.setVerticalGroup(
            e4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        f4p.setBackground(new java.awt.Color(204, 102, 0));
        f4p.setMaximumSize(new java.awt.Dimension(100, 100));
        f4p.setMinimumSize(new java.awt.Dimension(100, 100));
        f4p.setName(""); // NOI18N

        f4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f4.setMaximumSize(new java.awt.Dimension(70, 70));
        f4.setMinimumSize(new java.awt.Dimension(70, 70));
        f4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f4pLayout = new javax.swing.GroupLayout(f4p);
        f4p.setLayout(f4pLayout);
        f4pLayout.setHorizontalGroup(
            f4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        f4pLayout.setVerticalGroup(
            f4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g4p.setBackground(new java.awt.Color(255, 204, 102));

        g4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g4.setMaximumSize(new java.awt.Dimension(70, 70));
        g4.setMinimumSize(new java.awt.Dimension(70, 70));
        g4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g4pLayout = new javax.swing.GroupLayout(g4p);
        g4p.setLayout(g4pLayout);
        g4pLayout.setHorizontalGroup(
            g4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        g4pLayout.setVerticalGroup(
            g4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        h4p.setBackground(new java.awt.Color(204, 102, 0));

        h4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h4.setMaximumSize(new java.awt.Dimension(70, 70));
        h4.setMinimumSize(new java.awt.Dimension(70, 70));
        h4.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h4pLayout = new javax.swing.GroupLayout(h4p);
        h4p.setLayout(h4pLayout);
        h4pLayout.setHorizontalGroup(
            h4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        h4pLayout.setVerticalGroup(
            h4pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h4pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        a3p.setBackground(new java.awt.Color(204, 102, 0));

        a3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a3.setMaximumSize(new java.awt.Dimension(70, 70));
        a3.setMinimumSize(new java.awt.Dimension(70, 70));
        a3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a3pLayout = new javax.swing.GroupLayout(a3p);
        a3p.setLayout(a3pLayout);
        a3pLayout.setHorizontalGroup(
            a3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        a3pLayout.setVerticalGroup(
            a3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b3p.setBackground(new java.awt.Color(255, 204, 102));

        b3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b3.setMaximumSize(new java.awt.Dimension(70, 70));
        b3.setMinimumSize(new java.awt.Dimension(70, 70));
        b3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b3pLayout = new javax.swing.GroupLayout(b3p);
        b3p.setLayout(b3pLayout);
        b3pLayout.setHorizontalGroup(
            b3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        b3pLayout.setVerticalGroup(
            b3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c3p.setBackground(new java.awt.Color(204, 102, 0));

        c3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c3.setMaximumSize(new java.awt.Dimension(70, 70));
        c3.setMinimumSize(new java.awt.Dimension(70, 70));
        c3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c3pLayout = new javax.swing.GroupLayout(c3p);
        c3p.setLayout(c3pLayout);
        c3pLayout.setHorizontalGroup(
            c3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c3pLayout.setVerticalGroup(
            c3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d3p.setBackground(new java.awt.Color(255, 204, 102));
        d3p.setMaximumSize(new java.awt.Dimension(100, 100));
        d3p.setMinimumSize(new java.awt.Dimension(100, 100));
        d3p.setName(""); // NOI18N

        d3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d3.setMaximumSize(new java.awt.Dimension(70, 70));
        d3.setMinimumSize(new java.awt.Dimension(70, 70));
        d3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d3pLayout = new javax.swing.GroupLayout(d3p);
        d3p.setLayout(d3pLayout);
        d3pLayout.setHorizontalGroup(
            d3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d3pLayout.setVerticalGroup(
            d3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        e3p.setBackground(new java.awt.Color(204, 102, 0));

        e3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e3.setMaximumSize(new java.awt.Dimension(70, 70));
        e3.setMinimumSize(new java.awt.Dimension(70, 70));
        e3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e3pLayout = new javax.swing.GroupLayout(e3p);
        e3p.setLayout(e3pLayout);
        e3pLayout.setHorizontalGroup(
            e3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e3pLayout.setVerticalGroup(
            e3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        f3p.setBackground(new java.awt.Color(255, 204, 102));

        f3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f3.setMaximumSize(new java.awt.Dimension(70, 70));
        f3.setMinimumSize(new java.awt.Dimension(70, 70));
        f3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f3pLayout = new javax.swing.GroupLayout(f3p);
        f3p.setLayout(f3pLayout);
        f3pLayout.setHorizontalGroup(
            f3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        f3pLayout.setVerticalGroup(
            f3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g3p.setBackground(new java.awt.Color(204, 102, 0));

        g3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g3.setMaximumSize(new java.awt.Dimension(70, 70));
        g3.setMinimumSize(new java.awt.Dimension(70, 70));
        g3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g3pLayout = new javax.swing.GroupLayout(g3p);
        g3p.setLayout(g3pLayout);
        g3pLayout.setHorizontalGroup(
            g3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        g3pLayout.setVerticalGroup(
            g3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        h3p.setBackground(new java.awt.Color(255, 204, 102));

        h3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h3.setMaximumSize(new java.awt.Dimension(70, 70));
        h3.setMinimumSize(new java.awt.Dimension(70, 70));
        h3.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h3pLayout = new javax.swing.GroupLayout(h3p);
        h3p.setLayout(h3pLayout);
        h3pLayout.setHorizontalGroup(
            h3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        h3pLayout.setVerticalGroup(
            h3pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h3pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        a2p.setBackground(new java.awt.Color(255, 204, 102));
        a2p.setMaximumSize(new java.awt.Dimension(100, 100));
        a2p.setMinimumSize(new java.awt.Dimension(100, 100));
        a2p.setName(""); // NOI18N

        a2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a2.setMaximumSize(new java.awt.Dimension(70, 70));
        a2.setMinimumSize(new java.awt.Dimension(70, 70));
        a2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a2pLayout = new javax.swing.GroupLayout(a2p);
        a2p.setLayout(a2pLayout);
        a2pLayout.setHorizontalGroup(
            a2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        a2pLayout.setVerticalGroup(
            a2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b2p.setBackground(new java.awt.Color(204, 102, 0));

        b2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b2.setMaximumSize(new java.awt.Dimension(70, 70));
        b2.setMinimumSize(new java.awt.Dimension(70, 70));
        b2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b2pLayout = new javax.swing.GroupLayout(b2p);
        b2p.setLayout(b2pLayout);
        b2pLayout.setHorizontalGroup(
            b2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        b2pLayout.setVerticalGroup(
            b2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c2p.setBackground(new java.awt.Color(255, 204, 102));
        c2p.setMaximumSize(new java.awt.Dimension(100, 100));
        c2p.setMinimumSize(new java.awt.Dimension(100, 100));
        c2p.setName(""); // NOI18N

        c2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c2.setMaximumSize(new java.awt.Dimension(70, 70));
        c2.setMinimumSize(new java.awt.Dimension(70, 70));
        c2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c2pLayout = new javax.swing.GroupLayout(c2p);
        c2p.setLayout(c2pLayout);
        c2pLayout.setHorizontalGroup(
            c2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c2pLayout.setVerticalGroup(
            c2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d2p.setBackground(new java.awt.Color(204, 102, 0));

        d2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d2.setMaximumSize(new java.awt.Dimension(70, 70));
        d2.setMinimumSize(new java.awt.Dimension(70, 70));
        d2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d2pLayout = new javax.swing.GroupLayout(d2p);
        d2p.setLayout(d2pLayout);
        d2pLayout.setHorizontalGroup(
            d2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d2pLayout.setVerticalGroup(
            d2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        e2p.setBackground(new java.awt.Color(255, 204, 102));
        e2p.setMaximumSize(new java.awt.Dimension(100, 100));
        e2p.setMinimumSize(new java.awt.Dimension(100, 100));
        e2p.setName(""); // NOI18N

        e2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e2.setMaximumSize(new java.awt.Dimension(70, 70));
        e2.setMinimumSize(new java.awt.Dimension(70, 70));
        e2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e2pLayout = new javax.swing.GroupLayout(e2p);
        e2p.setLayout(e2pLayout);
        e2pLayout.setHorizontalGroup(
            e2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e2pLayout.setVerticalGroup(
            e2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        f2p.setBackground(new java.awt.Color(204, 102, 0));
        f2p.setMaximumSize(new java.awt.Dimension(100, 100));
        f2p.setMinimumSize(new java.awt.Dimension(100, 100));
        f2p.setName(""); // NOI18N

        f2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f2.setMaximumSize(new java.awt.Dimension(70, 70));
        f2.setMinimumSize(new java.awt.Dimension(70, 70));
        f2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f2pLayout = new javax.swing.GroupLayout(f2p);
        f2p.setLayout(f2pLayout);
        f2pLayout.setHorizontalGroup(
            f2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        f2pLayout.setVerticalGroup(
            f2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g2p.setBackground(new java.awt.Color(255, 204, 102));

        g2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g2.setMaximumSize(new java.awt.Dimension(70, 70));
        g2.setMinimumSize(new java.awt.Dimension(70, 70));
        g2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g2pLayout = new javax.swing.GroupLayout(g2p);
        g2p.setLayout(g2pLayout);
        g2pLayout.setHorizontalGroup(
            g2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        g2pLayout.setVerticalGroup(
            g2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        h2p.setBackground(new java.awt.Color(204, 102, 0));

        h2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h2.setMaximumSize(new java.awt.Dimension(70, 70));
        h2.setMinimumSize(new java.awt.Dimension(70, 70));
        h2.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h2pLayout = new javax.swing.GroupLayout(h2p);
        h2p.setLayout(h2pLayout);
        h2pLayout.setHorizontalGroup(
            h2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        h2pLayout.setVerticalGroup(
            h2pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h2pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        a1p.setBackground(new java.awt.Color(204, 102, 0));

        a1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a1.setMaximumSize(new java.awt.Dimension(70, 70));
        a1.setMinimumSize(new java.awt.Dimension(70, 70));
        a1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout a1pLayout = new javax.swing.GroupLayout(a1p);
        a1p.setLayout(a1pLayout);
        a1pLayout.setHorizontalGroup(
            a1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        a1pLayout.setVerticalGroup(
            a1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(a1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(a1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        b1p.setBackground(new java.awt.Color(255, 204, 102));

        b1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        b1.setMaximumSize(new java.awt.Dimension(70, 70));
        b1.setMinimumSize(new java.awt.Dimension(70, 70));
        b1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout b1pLayout = new javax.swing.GroupLayout(b1p);
        b1p.setLayout(b1pLayout);
        b1pLayout.setHorizontalGroup(
            b1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        b1pLayout.setVerticalGroup(
            b1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(b1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(b1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        c1p.setBackground(new java.awt.Color(204, 102, 0));

        c1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c1.setMaximumSize(new java.awt.Dimension(70, 70));
        c1.setMinimumSize(new java.awt.Dimension(70, 70));
        c1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout c1pLayout = new javax.swing.GroupLayout(c1p);
        c1p.setLayout(c1pLayout);
        c1pLayout.setHorizontalGroup(
            c1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        c1pLayout.setVerticalGroup(
            c1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(c1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(c1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        d1p.setBackground(new java.awt.Color(255, 204, 102));
        d1p.setMaximumSize(new java.awt.Dimension(100, 100));
        d1p.setMinimumSize(new java.awt.Dimension(100, 100));
        d1p.setName(""); // NOI18N

        d1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        d1.setMaximumSize(new java.awt.Dimension(70, 70));
        d1.setMinimumSize(new java.awt.Dimension(70, 70));
        d1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout d1pLayout = new javax.swing.GroupLayout(d1p);
        d1p.setLayout(d1pLayout);
        d1pLayout.setHorizontalGroup(
            d1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        d1pLayout.setVerticalGroup(
            d1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(d1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(d1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        e1p.setBackground(new java.awt.Color(204, 102, 0));

        e1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        e1.setMaximumSize(new java.awt.Dimension(70, 70));
        e1.setMinimumSize(new java.awt.Dimension(70, 70));
        e1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout e1pLayout = new javax.swing.GroupLayout(e1p);
        e1p.setLayout(e1pLayout);
        e1pLayout.setHorizontalGroup(
            e1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        e1pLayout.setVerticalGroup(
            e1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(e1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(e1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        f1p.setBackground(new java.awt.Color(255, 204, 102));

        f1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f1.setMaximumSize(new java.awt.Dimension(70, 70));
        f1.setMinimumSize(new java.awt.Dimension(70, 70));
        f1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout f1pLayout = new javax.swing.GroupLayout(f1p);
        f1p.setLayout(f1pLayout);
        f1pLayout.setHorizontalGroup(
            f1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        f1pLayout.setVerticalGroup(
            f1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(f1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(f1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        g1p.setBackground(new java.awt.Color(204, 102, 0));

        g1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        g1.setMaximumSize(new java.awt.Dimension(70, 70));
        g1.setMinimumSize(new java.awt.Dimension(70, 70));
        g1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout g1pLayout = new javax.swing.GroupLayout(g1p);
        g1p.setLayout(g1pLayout);
        g1pLayout.setHorizontalGroup(
            g1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        g1pLayout.setVerticalGroup(
            g1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(g1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(g1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        h1p.setBackground(new java.awt.Color(255, 204, 102));

        h1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        h1.setMaximumSize(new java.awt.Dimension(70, 70));
        h1.setMinimumSize(new java.awt.Dimension(70, 70));
        h1.setPreferredSize(new java.awt.Dimension(70, 70));

        javax.swing.GroupLayout h1pLayout = new javax.swing.GroupLayout(h1p);
        h1p.setLayout(h1pLayout);
        h1pLayout.setHorizontalGroup(
            h1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        h1pLayout.setVerticalGroup(
            h1pLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(h1pLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(h1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout PapanCaturLayout = new javax.swing.GroupLayout(PapanCatur);
        PapanCatur.setLayout(PapanCaturLayout);
        PapanCaturLayout.setHorizontalGroup(
            PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PapanCaturLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(a7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(a8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(c8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(c7p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PapanCaturLayout.createSequentialGroup()
                                .addComponent(d8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(e8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PapanCaturLayout.createSequentialGroup()
                                .addComponent(d7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(e7p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(f8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(g7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(g8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(h7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(h8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(a5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(a6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(c6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(c5p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PapanCaturLayout.createSequentialGroup()
                                .addComponent(d6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(e6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PapanCaturLayout.createSequentialGroup()
                                .addComponent(d5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(e5p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(f6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(g5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(g6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(h5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(h6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addComponent(a4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(b4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(d4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(e4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(f4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(g4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(h4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addComponent(a3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(b3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(d3p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(e3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(f3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(g3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(h3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(a1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(a2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(c2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(c1p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PapanCaturLayout.createSequentialGroup()
                                .addComponent(d2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(e2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PapanCaturLayout.createSequentialGroup()
                                .addComponent(d1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(e1p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(f2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(g1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(g2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(h1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(h2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PapanCaturLayout.setVerticalGroup(
            PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PapanCaturLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(a8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(c8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(e8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(d8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(a7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(d7p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(c7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(e7p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(f8p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(g8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(h8p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(f7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(g7p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(h7p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(a6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(c6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(e6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(d6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(a5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(d5p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(c5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(e5p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(f6p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(g6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(h6p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(f5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(g5p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(h5p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(a4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(b4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(c4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(e4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(d4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f4p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(g4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(h4p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(b3p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(a3p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(d3p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(c3p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(e3p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f3p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(g3p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(h3p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(a2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(b2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(c2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(e2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(d2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(a1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(d1p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(c1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(e1p, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PapanCaturLayout.createSequentialGroup()
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(f2p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(g2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(h2p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PapanCaturLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(f1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(g1p, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(h1p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        LogoUDINUS.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LogoUDINUS.setToolTipText("");

        Kalimat.setBackground(new java.awt.Color(51, 0, 255));

        Perintah.setFont(new java.awt.Font("Monotype Corsiva", 1, 48)); // NOI18N
        Perintah.setForeground(new java.awt.Color(255, 255, 255));
        Perintah.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Perintah.setText("Hello");

        javax.swing.GroupLayout KalimatLayout = new javax.swing.GroupLayout(Kalimat);
        Kalimat.setLayout(KalimatLayout);
        KalimatLayout.setHorizontalGroup(
            KalimatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(KalimatLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(Perintah, javax.swing.GroupLayout.PREFERRED_SIZE, 953, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );
        KalimatLayout.setVerticalGroup(
            KalimatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(KalimatLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(Perintah, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        Permainan.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Permainan", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N

        PanPilihWarna.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pilih Warna", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N

        buttonGroup1.add(RadHitam);
        RadHitam.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        RadHitam.setText("Hitam");
        RadHitam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadHitamActionPerformed(evt);
            }
        });

        buttonGroup1.add(RadPutih);
        RadPutih.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        RadPutih.setText("Putih");
        RadPutih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadPutihActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanPilihWarnaLayout = new javax.swing.GroupLayout(PanPilihWarna);
        PanPilihWarna.setLayout(PanPilihWarnaLayout);
        PanPilihWarnaLayout.setHorizontalGroup(
            PanPilihWarnaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihWarnaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanPilihWarnaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(RadHitam)
                    .addComponent(RadPutih))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        PanPilihWarnaLayout.setVerticalGroup(
            PanPilihWarnaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihWarnaLayout.createSequentialGroup()
                .addComponent(RadHitam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(RadPutih)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanManualMode.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manual Mode", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N

        TextManual.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        ButKirimData.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ButKirimData.setText("Kirim Data");
        ButKirimData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButKirimDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanManualModeLayout = new javax.swing.GroupLayout(PanManualMode);
        PanManualMode.setLayout(PanManualModeLayout);
        PanManualModeLayout.setHorizontalGroup(
            PanManualModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanManualModeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TextManual, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ButKirimData, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PanManualModeLayout.setVerticalGroup(
            PanManualModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanManualModeLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(PanManualModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TextManual)
                    .addComponent(ButKirimData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanPilihMode.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pilih Mode", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N

        buttonGroup2.add(RadManual);
        RadManual.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        RadManual.setText("Manual");
        RadManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadManualActionPerformed(evt);
            }
        });

        buttonGroup2.add(RadAutomatic);
        RadAutomatic.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        RadAutomatic.setSelected(true);
        RadAutomatic.setText("Automatic");
        RadAutomatic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadAutomaticActionPerformed(evt);
            }
        });

        buttonGroup2.add(RadClassic);
        RadClassic.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        RadClassic.setText("Classic");
        RadClassic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadClassicActionPerformed(evt);
            }
        });

        buttonGroup2.add(RadSemi);
        RadSemi.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        RadSemi.setText("Semi");
        RadSemi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadSemiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanPilihModeLayout = new javax.swing.GroupLayout(PanPilihMode);
        PanPilihMode.setLayout(PanPilihModeLayout);
        PanPilihModeLayout.setHorizontalGroup(
            PanPilihModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihModeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanPilihModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanPilihModeLayout.createSequentialGroup()
                        .addComponent(RadManual)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(RadClassic))
                    .addGroup(PanPilihModeLayout.createSequentialGroup()
                        .addComponent(RadAutomatic)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(RadSemi)
                        .addGap(0, 13, Short.MAX_VALUE)))
                .addContainerGap())
        );
        PanPilihModeLayout.setVerticalGroup(
            PanPilihModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihModeLayout.createSequentialGroup()
                .addGroup(PanPilihModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RadManual)
                    .addComponent(RadClassic))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanPilihModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RadAutomatic)
                    .addComponent(RadSemi))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanPilihEngine.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pilih Engine", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N

        ComboEngine.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        ButTambahkanEngine.setText("Tambahkan Engine");
        ButTambahkanEngine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButTambahkanEngineActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanPilihEngineLayout = new javax.swing.GroupLayout(PanPilihEngine);
        PanPilihEngine.setLayout(PanPilihEngineLayout);
        PanPilihEngineLayout.setHorizontalGroup(
            PanPilihEngineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihEngineLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanPilihEngineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ComboEngine, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PanPilihEngineLayout.createSequentialGroup()
                        .addGap(0, 144, Short.MAX_VALUE)
                        .addComponent(ButTambahkanEngine, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        PanPilihEngineLayout.setVerticalGroup(
            PanPilihEngineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihEngineLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ComboEngine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ButTambahkanEngine)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanPilihLevel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pilih Level Permainan", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N

        ComboLevel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ComboLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sulit", "Normal", "Mudah" }));

        ButTambahkanLevel.setText("Tambahkan Level");
        ButTambahkanLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButTambahkanLevelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanPilihLevelLayout = new javax.swing.GroupLayout(PanPilihLevel);
        PanPilihLevel.setLayout(PanPilihLevelLayout);
        PanPilihLevelLayout.setHorizontalGroup(
            PanPilihLevelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihLevelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanPilihLevelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ComboLevel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PanPilihLevelLayout.createSequentialGroup()
                        .addGap(0, 109, Short.MAX_VALUE)
                        .addComponent(ButTambahkanLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        PanPilihLevelLayout.setVerticalGroup(
            PanPilihLevelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanPilihLevelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ComboLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ButTambahkanLevel)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Aksi", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N

        ButLoad.setBackground(new java.awt.Color(153, 255, 153));
        ButLoad.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        ButLoad.setText("Load");
        ButLoad.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ButLoad.setMaximumSize(new java.awt.Dimension(40, 40));
        ButLoad.setMinimumSize(new java.awt.Dimension(40, 40));
        ButLoad.setPreferredSize(new java.awt.Dimension(40, 40));
        ButLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButLoadActionPerformed(evt);
            }
        });

        ButPrev.setBackground(new java.awt.Color(153, 255, 153));
        ButPrev.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        ButPrev.setText("Ulang");
        ButPrev.setEnabled(false);
        ButPrev.setMaximumSize(new java.awt.Dimension(40, 40));
        ButPrev.setMinimumSize(new java.awt.Dimension(40, 40));
        ButPrev.setPreferredSize(new java.awt.Dimension(40, 40));
        ButPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButPrevActionPerformed(evt);
            }
        });

        ButSave.setBackground(new java.awt.Color(153, 255, 153));
        ButSave.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        ButSave.setText("Simpan");
        ButSave.setEnabled(false);
        ButSave.setMaximumSize(new java.awt.Dimension(40, 40));
        ButSave.setMinimumSize(new java.awt.Dimension(40, 40));
        ButSave.setPreferredSize(new java.awt.Dimension(40, 40));

        ButPause.setBackground(new java.awt.Color(153, 255, 153));
        ButPause.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        ButPause.setText("Tunda");
        ButPause.setEnabled(false);
        ButPause.setMaximumSize(new java.awt.Dimension(40, 40));
        ButPause.setMinimumSize(new java.awt.Dimension(40, 40));
        ButPause.setPreferredSize(new java.awt.Dimension(40, 40));
        ButPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButPauseActionPerformed(evt);
            }
        });

        ButMulai.setBackground(java.awt.Color.green);
        ButMulai.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        ButMulai.setText("Mulai");
        ButMulai.setAlignmentX(10.0F);
        ButMulai.setAlignmentY(10.0F);
        ButMulai.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.black, null, null));
        ButMulai.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ButMulai.setMargin(new java.awt.Insets(10, 10, 10, 10));
        ButMulai.setMaximumSize(new java.awt.Dimension(86, 86));
        ButMulai.setMinimumSize(new java.awt.Dimension(86, 86));
        ButMulai.setPreferredSize(new java.awt.Dimension(86, 86));
        ButMulai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButMulaiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ButPrev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ButLoad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ButSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ButPause, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ButMulai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ButSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ButLoad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ButPause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ButPrev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(ButMulai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout PermainanLayout = new javax.swing.GroupLayout(Permainan);
        Permainan.setLayout(PermainanLayout);
        PermainanLayout.setHorizontalGroup(
            PermainanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PermainanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PanManualMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanPilihMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanPilihWarna, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanPilihEngine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanPilihLevel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        PermainanLayout.setVerticalGroup(
            PermainanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PermainanLayout.createSequentialGroup()
                .addGroup(PermainanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(PermainanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(PanPilihLevel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(PanPilihEngine, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(PanManualMode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(PanPilihWarna, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(PanPilihMode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setMaximumSize(new java.awt.Dimension(936, 102));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(936, 102));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(936, 102));

        TabelPermainan.setAutoCreateRowSorter(true);
        TabelPermainan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        TabelPermainan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pemain", "Komputer"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TabelPermainan.setAlignmentX(2.0F);
        TabelPermainan.setAlignmentY(2.0F);
        TabelPermainan.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        TabelPermainan.setRowHeight(50);
        jScrollPane1.setViewportView(TabelPermainan);

        WaktuPemain.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        WaktuPemain.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        WaktuPemain.setText("15:00");
        WaktuPemain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WaktuPemainActionPerformed(evt);
            }
        });

        WaktuKomputer.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        WaktuKomputer.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        WaktuKomputer.setText("15:00");
        WaktuKomputer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WaktuKomputerActionPerformed(evt);
            }
        });

        CountKomputer.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        CountKomputer.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CountKomputer.setText("00:00");
        CountKomputer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CountKomputerActionPerformed(evt);
            }
        });

        CountPemain.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        CountPemain.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CountPemain.setText("00:00");

        LabelWarnaPemain.setBackground(new java.awt.Color(51, 51, 255));
        LabelWarnaPemain.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelWarnaPemain.setMaximumSize(new java.awt.Dimension(70, 70));
        LabelWarnaPemain.setMinimumSize(new java.awt.Dimension(70, 70));
        LabelWarnaPemain.setPreferredSize(new java.awt.Dimension(70, 70));

        LabelWarnaKomputer.setBackground(new java.awt.Color(255, 51, 51));
        LabelWarnaKomputer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelWarnaKomputer.setMaximumSize(new java.awt.Dimension(70, 70));
        LabelWarnaKomputer.setMinimumSize(new java.awt.Dimension(70, 70));
        LabelWarnaKomputer.setPreferredSize(new java.awt.Dimension(70, 70));

        TextDebug.setBackground(new java.awt.Color(0, 0, 0));
        TextDebug.setColumns(20);
        TextDebug.setForeground(new java.awt.Color(255, 255, 255));
        TextDebug.setRows(5);
        TextDebug.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        PanelDebug.setViewportView(TextDebug);

        TextTimerTotal.setFont(new java.awt.Font("Segoe UI", 1, 64)); // NOI18N
        TextTimerTotal.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TextTimerTotal.setText("15:00");

        ButSetTime.setBackground(new java.awt.Color(255, 255, 204));
        ButSetTime.setForeground(new java.awt.Color(255, 255, 204));
        ButSetTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButSetTimeActionPerformed(evt);
            }
        });

        JtextFEN.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        JtextFEN.setText("FEN STRING");
        JtextFEN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JtextFENActionPerformed(evt);
            }
        });

        jFENUpdate.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jFENUpdate.setText("UPDATE FEN");
        jFENUpdate.setToolTipText("");
        jFENUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFENUpdateActionPerformed(evt);
            }
        });

        jStartFEN.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jStartFEN.setText("SET START");
        jStartFEN.setToolTipText("");
        jStartFEN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartFENActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Promote"));

        ButPromote.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        ButPromote.setText("OK");
        ButPromote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButPromoteActionPerformed(evt);
            }
        });

        buttonGroup4.add(RadBenteng);
        RadBenteng.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        RadBenteng.setText("Benteng");
        RadBenteng.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadBentengActionPerformed(evt);
            }
        });

        buttonGroup4.add(RadSter);
        RadSter.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        RadSter.setText("Mentri");
        RadSter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadSterActionPerformed(evt);
            }
        });

        buttonGroup4.add(RadMentri);
        RadMentri.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        RadMentri.setText("Gajah");
        RadMentri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadMentriActionPerformed(evt);
            }
        });

        buttonGroup4.add(RadKuda);
        RadKuda.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        RadKuda.setText("Kuda");
        RadKuda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadKudaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(RadMentri)
                .addGap(26, 26, 26)
                .addComponent(RadKuda)
                .addGap(28, 28, 28)
                .addComponent(RadBenteng)
                .addGap(98, 98, 98)
                .addComponent(ButPromote)
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(RadSter)
                    .addContainerGap(518, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ButPromote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(RadBenteng)
                    .addComponent(RadKuda)
                    .addComponent(RadMentri))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(RadSter)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(PapanCatur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ButSetTime, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TextTimerTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(LogoUDINUS, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(Kalimat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(LabelWarnaPemain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(WaktuPemain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(32, 32, 32)
                                                .addComponent(WaktuKomputer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(32, 32, 32)
                                                .addComponent(LabelWarnaKomputer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(CountPemain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(32, 32, 32)
                                                .addComponent(CountKomputer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(PanelDebug, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(JtextFEN, javax.swing.GroupLayout.PREFERRED_SIZE, 676, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jFENUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jStartFEN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Arduino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Permainan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(LogoUDINUS, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(ButSetTime, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TextTimerTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Kalimat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(LabelWarnaKomputer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(WaktuPemain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(WaktuKomputer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(CountPemain, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(CountKomputer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(LabelWarnaPemain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(PanelDebug)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jStartFEN)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(JtextFEN, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jFENUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(PapanCatur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Arduino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Permainan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void KoneksikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KoneksikanActionPerformed
        // TODO add your handling code here:
        if(dataButKonek == 0){
            try{
                SerialPort []portLists = SerialPort.getCommPorts();
                SerialPort1 = portLists[jComboCOM.getSelectedIndex()];
                SerialPort1.setBaudRate(115200);
                SerialPort1.setNumDataBits(8);
                SerialPort1.setNumStopBits(1);
                //SerialPort1.setParity();
                SerialPort1.openPort();
                if(SerialPort1.isOpen()){
                    JOptionPane.showMessageDialog(this, SerialPort1.getDescriptivePortName() + " -- Berhasil Terkoneksi Dengan Arduino");
                    jComboCOM.setEnabled(false);
                    Koneksikan.setText("Putuskan");
                    dataButKonek=1;
                }
                else{
                    JOptionPane.showMessageDialog(this, SerialPort1.getDescriptivePortName() + " -- Gagal Terkoneksi Dengan Arduino");
                }
            }
            catch(ArrayIndexOutOfBoundsException a){
                JOptionPane.showMessageDialog(this, " -- Pilih COM Port Terlebih Dahulu --", "ERROR",ERROR_MESSAGE);
            }
            catch(Exception b){
                JOptionPane.showMessageDialog(this,b, "ERROR",ERROR_MESSAGE);
            }
        }
        else{
            if(SerialPort1.isOpen()){
              SerialPort1.closePort();
              jComboCOM.setEnabled(true);
              Koneksikan.setText("Koneksikan");
              dataButKonek=0;
              JOptionPane.showMessageDialog(this, SerialPort1.getDescriptivePortName() + " -- Berhasil Memutus Koneksi Dengan Arduino");
            }
        }
    }//GEN-LAST:event_KoneksikanActionPerformed

    private void ButMulaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButMulaiActionPerformed
        // TODO add your handling code here:   
        if("START".equals(STARTPOINT)){
            int respon = JOptionPane.showConfirmDialog(this, "Apakah Anda Yakin Akan Memulai Permainan ?", "KONFIRMASI",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); 
            if(respon == JOptionPane.YES_OPTION){
                if(RadClassic.isSelected()){
                    JOptionPane.showMessageDialog(this, "Untuk Permainan Mode Classic Tunggu Update Selanjutnya dari UDINUS!!"); 
                }
                else if(!RadHitam.isSelected() && !RadPutih.isSelected()){
                    JOptionPane.showMessageDialog(this, "Anda Belum Memilih Warna Yang Ingin Dimainkan!!"); 
                }
                else{
                    //JtextFEN.setText(FENSTARTGAME);
                    //board.loadFromFen(FENSTARTGAME);
                    String SideTime = String.valueOf(board.getSideToMove());
                    //System.out.println(SideTime);
                    if(RadHitam.isSelected()) {
                        if(SideTime.equals("WHITE")){
                            PrintWayahe("KOMPUTER");
                            ButPrev.setEnabled(false);
                        }
                        else{
                            PrintWayahe("PEMAIN");
                            ButPrev.setEnabled(false);
                        }
                    }
                    else if(RadPutih.isSelected()) {
                        if(SideTime.equals("WHITE")){
                            PrintWayahe("PEMAIN");
                            ButPrev.setEnabled(false);
                        }
                        else{
                            PrintWayahe("KOMPUTER");
                            ButPrev.setEnabled(false);
                        }
                    }
                    STARTPOINT = "STOP";
                    ResizeIconButton(STOPICON.getImage(),ButMulai);
                    ButMulai.setBackground(Color.red);
                    ButLoad.setEnabled(false);
                    ButSave.setEnabled(true);
                    //ButPrev.setEnabled(true);
                    ButPause.setEnabled(true);
                    RadHitam.setEnabled(false);
                    RadPutih.setEnabled(false);
                    RadAutomatic.setEnabled(false);
                    RadManual.setEnabled(false);
                    RadClassic.setEnabled(false);
                    RadSemi.setEnabled(false);
                    ComboEngine.setEnabled(false);
                    ComboLevel.setEnabled(false);
                    ButTambahkanEngine.setEnabled(false);
                    ButTambahkanLevel.setEnabled(false);
                    jFENUpdate.setEnabled(false);
                    JtextFEN.setEnabled(false);
                    jStartFEN.setEnabled(false);
                    if(RadManual.isSelected() || RadSemi.isSelected()){
                        ButKirimData.setEnabled(true);
                    }
                    timer1.start();
                    if(RadPutih.isSelected()) timerPemain.start();
                    else timerKomputer.start();
                    TextDebug.setText("");
                    TextDebug.setText("-------------***MEMULAI GAME BARU***-------------\n");
                    SetDefaultAllTimerText();
                    JtextFEN.setText(FENSTARTGAME);
                    board.loadFromFen(FENSTARTGAME);
                    firstPlay = 1;
                    //board.lo
                    //TextDebug.append("SEMANGATH BOSQUE");
                    /*try {
                        Process process = Runtime.getRuntime().exec(getClass().getResource("/ProgramUtama/Stockfish15/stockfish_15_x64_avx2.exe"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }*/
                    //System.out.println(board.toString());
                }
            }
        }
        else{ //stop
            int respon = JOptionPane.showConfirmDialog(this, "Apakah Anda Yakin Akan Menghentikan Permainan ?", "KONFIRMASI",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); 
            if(respon == JOptionPane.YES_OPTION){
                timer1.stop();
                timerPemain.stop();
                timerKomputer.stop();
                PrintWayahe("AWAL");
                STARTPOINT = "START";
                ResizeIconButton(STARTICON.getImage(),ButMulai);
                ButMulai.setBackground(Color.green);
                ButLoad.setEnabled(true);
                ButSave.setEnabled(false);
                ButPrev.setEnabled(false);
                ButPause.setEnabled(false);
                RadHitam.setEnabled(true);
                RadPutih.setEnabled(true);
                RadAutomatic.setEnabled(true);
                RadManual.setEnabled(true);
                RadClassic.setEnabled(true);
                RadSemi.setEnabled(true);
                ComboEngine.setEnabled(true);
                ComboLevel.setEnabled(true);
                ButTambahkanEngine.setEnabled(true);
                ButTambahkanLevel.setEnabled(true);
                ButKirimData.setEnabled(false);
                TextManual.setText("");
                jFENUpdate.setEnabled(true);
                JtextFEN.setEnabled(true);
                jStartFEN.setEnabled(true);
                //board = new Board();
                FENSTARTGAME=JtextFEN.getText();
                movesRecord = "";
                //FEN=STARTFEN;
                counterPlay=0;
                if(RadPutih.isSelected()) GoPoint(FEN,"putih");
                else GoPoint(FEN,"hitam");
                PEMAINMENIT = SETMENIT;
                PEMAINDETIK = SETDETIK;
                KOMPUTERMENIT = SETMENIT;
                KOMPUTERDETIK = SETDETIK;
                DataBuffer = "";
                SetDefaultAllTimerText();
                DefaultTableModel model = (DefaultTableModel) TabelPermainan.getModel();           
                for (int i = model.getRowCount() - 1; i >= 0; i--) model.removeRow(i);
            }
        }
    }//GEN-LAST:event_ButMulaiActionPerformed

    private void RadHitamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadHitamActionPerformed
        // TODO add your handling code here:  
        FEN = board.getFen();
        GoPoint(FEN,"hitam");
        LabelWarnaPemain.setIcon(BRaja);
        LabelWarnaKomputer.setIcon(WRaja);
        // --- TAMBAHAN KIRIM SERIAL (UBAH WARNA HITAM) ---
        if(RadAutomatic.isSelected() || RadSemi.isSelected()){
            try{
                outputStream1 = SerialPort1.getOutputStream();
                String dataToSend = "SET_WARNA:HITAM\n";
                outputStream1.write(dataToSend.getBytes());
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }//GEN-LAST:event_RadHitamActionPerformed

    private void RadPutihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadPutihActionPerformed
        // TODO add your handling code here:
        FEN = board.getFen();
        GoPoint(FEN,"putih");
        LabelWarnaPemain.setIcon(WRaja);
        LabelWarnaKomputer.setIcon(BRaja);
        if(RadAutomatic.isSelected() || RadSemi.isSelected()){
            try{
                outputStream1 = SerialPort1.getOutputStream();
                String dataToSend = "SET_WARNA:PUTIH\n";
                outputStream1.write(dataToSend.getBytes());
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }//GEN-LAST:event_RadPutihActionPerformed

    private void ButKirimDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButKirimDataActionPerformed
        // TODO add your handling code here:
        TextTerima.setText(TextManual.getText());
    }//GEN-LAST:event_ButKirimDataActionPerformed

    private void RadManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadManualActionPerformed
        // TODO add your handling code here:
        TextManual.setEditable(true);
        MODE = "MANUAL";
        //ButKirimData.setEnabled(true);
    }//GEN-LAST:event_RadManualActionPerformed

    private void RadAutomaticActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadAutomaticActionPerformed
        // TODO add your handling code here:
        TextManual.setEditable(false);
        MODE = "AUTOMATIC";
        //ButKirimData.setEnabled(false);
    }//GEN-LAST:event_RadAutomaticActionPerformed

    private void ButPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButPauseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ButPauseActionPerformed

    private void ButPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButPrevActionPerformed
        // TODO add your handling code here:
        int Panjang = movesRecord.length();
        //TextManual.setText(String.valueOf(Panjang));
        if(Panjang < 6){
            board = new Board();
            movesRecord = "";
            FEN=STARTFEN;
            counterPlay=0;
            if(RadPutih.isSelected()) GoPoint(FEN,"putih");
            else GoPoint(FEN,"hitam");
            PEMAINMENIT = SETMENIT;
            PEMAINDETIK = SETDETIK;
            KOMPUTERMENIT = SETMENIT;
            KOMPUTERDETIK = SETDETIK;
            SetDefaultAllTimerText();
            DefaultTableModel model = (DefaultTableModel) TabelPermainan.getModel();           
            model.removeRow(0);
            if(RadHitam.isSelected()) WAYAHE = "KOMPUTER";
            else WAYAHE = "PEMAIN";
            PrintWayahe(WAYAHE);
        }
        else{
            if(RadHitam.isSelected()){
                DefaultTableModel model = (DefaultTableModel) TabelPermainan.getModel();           
                model.removeRow(counterPlay);
                model.removeRow(counterPlay-1);
                //model.addRow(new Object[] {" ", bestMoving});
                counterPlay--;
                //TextManual.setText(movesRecord);
                
                /*String[] result = movesRecord.trim().split("\s");
                movesRecord = movesRecord.substring(0,movesRecord.length()-10);
                model.addRow(new Object[] {" ", movesRecord.substring(movesRecord.length()-4,movesRecord.length())});
                */
                String[] result = movesRecord.trim().split("\s");
                movesRecord = " ";
                for(int i =0 ;i< result.length - 2 ; i++){
                   movesRecord += result[i];
                   if(i < result.length - 3) movesRecord += " ";
                   else model.addRow(new Object[] {" ", result[i]}); 
                }
                
                //TextManual.setText(movesRecord);
                board.undoMove();
                board.undoMove();
                //System.out.println();
                FEN = board.getFen();
                if(RadPutih.isSelected()) GoPoint(FEN,"putih");
                else GoPoint(FEN,"hitam");
                JtextFEN.setText(FEN);
            }
            else{
                DefaultTableModel model = (DefaultTableModel) TabelPermainan.getModel();           
                model.removeRow(counterPlay-1);
                //model.removeRow(counterPlay-1);
                //model.addRow(new Object[] {" ", bestMoving});
                counterPlay--;
                //TextManual.setText(movesRecord);
                
                /*String[] result = movesRecord.trim().split("\s");
                movesRecord = movesRecord.substring(0,movesRecord.length()-10);
                model.addRow(new Object[] {" ", movesRecord.substring(movesRecord.length()-4,movesRecord.length())});
                */
                String[] result = movesRecord.trim().split("\s");
                movesRecord = " ";
                for(int i =0 ;i< result.length - 2 ; i++){
                   movesRecord += result[i];
                   if(i < result.length - 3) movesRecord += " ";
                   //else model.addRow(new Object[] {" ", result[i]}); 
                }
                
                //TextManual.setText(movesRecord);
                board.undoMove();
                board.undoMove();
                //System.out.println();
                FEN = board.getFen();
                if(RadPutih.isSelected()) GoPoint(FEN,"putih");
                else GoPoint(FEN,"hitam");
                JtextFEN.setText(FEN);
            }
        }
        TextDebug.append("Undo Move\n");
    }//GEN-LAST:event_ButPrevActionPerformed

    private void RadClassicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadClassicActionPerformed
        // TODO add your handling code here:
        TextManual.setEditable(false);
        MODE = "CLASSIC";
    }//GEN-LAST:event_RadClassicActionPerformed

    private void WaktuKomputerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WaktuKomputerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_WaktuKomputerActionPerformed

    private void ButTambahkanEngineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButTambahkanEngineActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this, "Tunggu Update Selanjutnya dari UDINUS!!");
    }//GEN-LAST:event_ButTambahkanEngineActionPerformed

    private void ButTambahkanLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButTambahkanLevelActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this, "Tunggu Update Selanjutnya dari UDINUS!!");
    }//GEN-LAST:event_ButTambahkanLevelActionPerformed

    private void ButLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButLoadActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this, "Tunggu Update Selanjutnya dari UDINUS!!");
    }//GEN-LAST:event_ButLoadActionPerformed

    private void TextKirimInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_TextKirimInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_TextKirimInputMethodTextChanged

    private void TextTerimaInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_TextTerimaInputMethodTextChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_TextTerimaInputMethodTextChanged

    private void TextTerimaPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_TextTerimaPropertyChange
        // TODO add your handling code here:
        
    }//GEN-LAST:event_TextTerimaPropertyChange

    private void TextTerimaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TextTerimaKeyReleased
        // TODO add your handling code here:
        
    }//GEN-LAST:event_TextTerimaKeyReleased

    private void TextTerimaVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_TextTerimaVetoableChange
        // TODO add your handling code here:
    }//GEN-LAST:event_TextTerimaVetoableChange

    private void TextTerimaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextTerimaActionPerformed
        // TODO add your handling code here:
        //Perintah.setText(TextTerima.getText());
       // TextTerima.setText("");
    }//GEN-LAST:event_TextTerimaActionPerformed

    private void CountKomputerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CountKomputerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CountKomputerActionPerformed

    private void WaktuPemainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WaktuPemainActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_WaktuPemainActionPerformed

    private void ButSetTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButSetTimeActionPerformed
        // TODO add your handling code here:
        String NewString = TextTimerTotal.getText();
        char[] ch = NewString.toCharArray();
        if(CekIfDigit(ch[0]) && CekIfDigit(ch[1]) && ch[2] == ':' && CekIfDigit(ch[3]) && CekIfDigit(ch[4]) && NewString.length() < 6){
            int respon = JOptionPane.showConfirmDialog(this, "Apakah Anda Yakin Akan Mengganti Waktu Permainan ?", "KONFIRMASI",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); 
            if(respon == JOptionPane.YES_OPTION){
                SETMENIT = Integer.parseInt(""+ch[0]+ch[1]);
                SETDETIK = Integer.parseInt(""+ch[3]+ch[4]);
                SetDefaultAllTimerText();
            }
            else{ //tidak jadi ganti
                SetDefaultAllTimerText();
            }
        }
        else{ //format salah
            JOptionPane.showMessageDialog(this, "Format Waktu Anda Salah!! MM:DD");
            SetDefaultAllTimerText();
        }
    }//GEN-LAST:event_ButSetTimeActionPerformed

    private void jComboCOMPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jComboCOMPopupMenuWillBecomeVisible
        // TODO add your handling code here:
        jComboCOM.removeAllItems();
        SerialPort []portLists = SerialPort.getCommPorts();
        for(SerialPort port : portLists){
            jComboCOM.addItem(port.getSystemPortName());
        }
    }//GEN-LAST:event_jComboCOMPopupMenuWillBecomeVisible

    private void RadSemiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadSemiActionPerformed
        // TODO add your handling code here:
        TextManual.setEditable(true);
        MODE = "SEMI";
    }//GEN-LAST:event_RadSemiActionPerformed

    private void jFENUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFENUpdateActionPerformed
        // TODO add your handling code here:
        int errorFEN=0;
        try{
            board.loadFromFen(JtextFEN.getText());
        }
        catch(Exception b){
            errorFEN=1;
            JOptionPane.showMessageDialog(this, "Input FEN STRING ANDA SALAH");
        }
        if(errorFEN == 0){
            FENSTARTGAME = JtextFEN.getText();
            FEN=FENSTARTGAME;
        }
        movesRecord = "";
        counterPlay=0;
        if(RadPutih.isSelected()) GoPoint(FEN,"putih");
        else GoPoint(FEN,"hitam");
        JtextFEN.setText(FENSTARTGAME);
    }//GEN-LAST:event_jFENUpdateActionPerformed

    private void JtextFENActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JtextFENActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_JtextFENActionPerformed

    private void jStartFENActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartFENActionPerformed
        // TODO add your handling code here:
        FEN=STARTFEN;
        FENSTARTGAME = STARTFEN;
        board = new Board();
        movesRecord = "";
        FEN=STARTFEN;
        counterPlay=0;
        if(RadPutih.isSelected()) GoPoint(FEN,"putih");
        else GoPoint(FEN,"hitam");
        JtextFEN.setText(FENSTARTGAME);
        
        // --- TAMBAHAN KIRIM SERIAL (RESET GAME) ---
        if(RadAutomatic.isSelected() || RadSemi.isSelected()){
            try{
                outputStream1 = SerialPort1.getOutputStream();
                String dataToSend = "RESET_GAME\n";
                outputStream1.write(dataToSend.getBytes());
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
        
    }//GEN-LAST:event_jStartFENActionPerformed

    private void ButPromoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButPromoteActionPerformed
        // TODO add your handling code here:
        DataPromote = 0;
    }//GEN-LAST:event_ButPromoteActionPerformed

    private void RadBentengActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadBentengActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RadBentengActionPerformed

    private void RadSterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadSterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RadSterActionPerformed

    private void RadMentriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadMentriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RadMentriActionPerformed

    private void RadKudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadKudaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RadKudaActionPerformed
    
    private void SetDefaultAllTimerText(){
        TextTimerTotal.setText(TampilTimer(SETMENIT,SETDETIK));
        WaktuPemain.setText(TampilTimer(SETMENIT,SETDETIK));
        WaktuKomputer.setText(TampilTimer(SETMENIT,SETDETIK));
        CountPemain.setText(TampilTimer(0,0));
        CountKomputer.setText(TampilTimer(0,0));
    }
    private String TampilTimer(int menit, int detik){
        String NewString = null;
        String Sdetik,Smenit;
        if(detik < 10){
          Sdetik = "0"+detik;
        }
        else if(detik>60){
          menit++;
          detik = detik - 60;
          Sdetik = ""+detik;
        }
        else{
          Sdetik = ""+detik;  
        }
        
        if(menit < 10){
          Smenit = "0"+menit;
        }
        else{
          Smenit = ""+menit;  
        }
        NewString = Smenit+":"+Sdetik;
        return NewString;
    } 
    
    private void GoPoint(String dataFEN, String warna){
        if(warna.equals("putih")) {
            String[] ParsingFEN;
            
            ParsingFEN = dataFEN.split("/");
            //cek Hasil
            for(int poin=0;poin<8;poin++){
                //ImageIcon Drawa=null,Drawb=null,Drawc=null,Drawd=null,Drawe=null,Drawf=null,Drawg=null,Drawh=null;
                ImageIcon[] Draw={null,null,null,null,null,null,null,null};
                int pp=0;
                String HasilFEN="";
                while (pp < ParsingFEN[poin].length()){ //mengubah angka menjadi jumlah x
                    if(Character.isDigit(ParsingFEN[poin].charAt(pp))){
                        switch (ParsingFEN[poin].charAt(pp)){
                            case '1' -> HasilFEN=HasilFEN+"x";
                            case '2' -> HasilFEN=HasilFEN+"xx";
                            case '3' -> HasilFEN=HasilFEN+"xxx";
                            case '4' -> HasilFEN=HasilFEN+"xxxx";
                            case '5' -> HasilFEN=HasilFEN+"xxxxx";
                            case '6' -> HasilFEN=HasilFEN+"xxxxxx";
                            case '7' -> HasilFEN=HasilFEN+"xxxxxxx";
                            case '8' -> HasilFEN=HasilFEN+"xxxxxxxx";
                            default ->  {}
                        }
                    }
                    else{
                        HasilFEN=HasilFEN + Character.toString(ParsingFEN[poin].charAt(pp));
                    }
                    pp++;
                }
                char[] ch = HasilFEN.toCharArray();
                for(int kk=0;kk<8;kk++){
                    Draw[kk] = setGambar(ch[kk]);
                }
                Set(poin, Draw[0],Draw[1],Draw[2],Draw[3],Draw[4],Draw[5],Draw[6],Draw[7]);
            }
            //System.out.println("ini Putih");
        }
        else{
            String[] ParsingFEN;
            
            ParsingFEN = dataFEN.split("/");
            //cek Hasil
            for(int poin=0;poin<8;poin++){
                ImageIcon[] Draw={null,null,null,null,null,null,null,null};
                int pp=0;
                String HasilFEN="";
                while (pp < ParsingFEN[poin].length()){ //mengubah angka menjadi jumlah x
                    if(Character.isDigit(ParsingFEN[poin].charAt(pp))){
                        switch (ParsingFEN[poin].charAt(pp)){
                            case '1' -> HasilFEN=HasilFEN+"x";
                            case '2' -> HasilFEN=HasilFEN+"xx";
                            case '3' -> HasilFEN=HasilFEN+"xxx";
                            case '4' -> HasilFEN=HasilFEN+"xxxx";
                            case '5' -> HasilFEN=HasilFEN+"xxxxx";
                            case '6' -> HasilFEN=HasilFEN+"xxxxxx";
                            case '7' -> HasilFEN=HasilFEN+"xxxxxxx";
                            case '8' -> HasilFEN=HasilFEN+"xxxxxxxx";
                            default ->  {}
                        }
                    }
                    else{
                        HasilFEN=HasilFEN + Character.toString(ParsingFEN[poin].charAt(pp));
                    }
                    pp++;
                }
                char[] ch = HasilFEN.toCharArray();
                for(int kk=7;kk >-1;kk--){
                    Draw[kk] = setGambar(ch[kk]);
                }
                SetHitam(poin, Draw[0],Draw[1],Draw[2],Draw[3],Draw[4],Draw[5],Draw[6],Draw[7]);   
                
            }
            //System.out.println("ini Hitam");
        }
    }
    
    private void Set(int point, ImageIcon Drawa, ImageIcon Drawb, ImageIcon Drawc, ImageIcon Drawd, ImageIcon Drawe,ImageIcon Drawf, ImageIcon Drawg,ImageIcon Drawh){
        switch (point) {
            case 7 -> {
                a1.setIcon(Drawa);
                b1.setIcon(Drawb);
                c1.setIcon(Drawc);
                d1.setIcon(Drawd);
                e1.setIcon(Drawe);
                f1.setIcon(Drawf);
                g1.setIcon(Drawg);
                h1.setIcon(Drawh);
            }
            case 6 -> {
                a2.setIcon(Drawa);
                b2.setIcon(Drawb);
                c2.setIcon(Drawc);
                d2.setIcon(Drawd);
                e2.setIcon(Drawe);
                f2.setIcon(Drawf);
                g2.setIcon(Drawg);
                h2.setIcon(Drawh);
            }
            case 5 -> {
                a3.setIcon(Drawa);
                b3.setIcon(Drawb);
                c3.setIcon(Drawc);
                d3.setIcon(Drawd);
                e3.setIcon(Drawe);
                f3.setIcon(Drawf);
                g3.setIcon(Drawg);
                h3.setIcon(Drawh);
            }
            case 4 -> {
                a4.setIcon(Drawa);
                b4.setIcon(Drawb);
                c4.setIcon(Drawc);
                d4.setIcon(Drawd);
                e4.setIcon(Drawe);
                f4.setIcon(Drawf);
                g4.setIcon(Drawg);
                h4.setIcon(Drawh);
            }
            case 3 -> {
                a5.setIcon(Drawa);
                b5.setIcon(Drawb);
                c5.setIcon(Drawc);
                d5.setIcon(Drawd);
                e5.setIcon(Drawe);
                f5.setIcon(Drawf);
                g5.setIcon(Drawg);
                h5.setIcon(Drawh);
            }
            case 2 -> {
                a6.setIcon(Drawa);
                b6.setIcon(Drawb);
                c6.setIcon(Drawc);
                d6.setIcon(Drawd);
                e6.setIcon(Drawe);
                f6.setIcon(Drawf);
                g6.setIcon(Drawg);
                h6.setIcon(Drawh);
            }
            case 1 -> {
                a7.setIcon(Drawa);
                b7.setIcon(Drawb);
                c7.setIcon(Drawc);
                d7.setIcon(Drawd);
                e7.setIcon(Drawe);
                f7.setIcon(Drawf);
                g7.setIcon(Drawg);
                h7.setIcon(Drawh);
            }
            case 0 -> {
                a8.setIcon(Drawa);
                b8.setIcon(Drawb);
                c8.setIcon(Drawc);
                d8.setIcon(Drawd);
                e8.setIcon(Drawe);
                f8.setIcon(Drawf);
                g8.setIcon(Drawg);
                h8.setIcon(Drawh);
            }
            default -> {
            }
        }
    }
    
    private void SetHitam(int point, ImageIcon Drawh, ImageIcon Drawg, ImageIcon Drawf, ImageIcon Drawe, ImageIcon Drawd,ImageIcon Drawc, ImageIcon Drawb,ImageIcon Drawa){
        switch (point) {
            case 0 -> {
                a1.setIcon(Drawa);
                b1.setIcon(Drawb);
                c1.setIcon(Drawc);
                d1.setIcon(Drawd);
                e1.setIcon(Drawe);
                f1.setIcon(Drawf);
                g1.setIcon(Drawg);
                h1.setIcon(Drawh);
            }
            case 1 -> {
                a2.setIcon(Drawa);
                b2.setIcon(Drawb);
                c2.setIcon(Drawc);
                d2.setIcon(Drawd);
                e2.setIcon(Drawe);
                f2.setIcon(Drawf);
                g2.setIcon(Drawg);
                h2.setIcon(Drawh);
            }
            case 2 -> {
                a3.setIcon(Drawa);
                b3.setIcon(Drawb);
                c3.setIcon(Drawc);
                d3.setIcon(Drawd);
                e3.setIcon(Drawe);
                f3.setIcon(Drawf);
                g3.setIcon(Drawg);
                h3.setIcon(Drawh);
            }
            case 3 -> {
                a4.setIcon(Drawa);
                b4.setIcon(Drawb);
                c4.setIcon(Drawc);
                d4.setIcon(Drawd);
                e4.setIcon(Drawe);
                f4.setIcon(Drawf);
                g4.setIcon(Drawg);
                h4.setIcon(Drawh);
            }
            case 4 -> {
                a5.setIcon(Drawa);
                b5.setIcon(Drawb);
                c5.setIcon(Drawc);
                d5.setIcon(Drawd);
                e5.setIcon(Drawe);
                f5.setIcon(Drawf);
                g5.setIcon(Drawg);
                h5.setIcon(Drawh);
            }
            case 5 -> {
                a6.setIcon(Drawa);
                b6.setIcon(Drawb);
                c6.setIcon(Drawc);
                d6.setIcon(Drawd);
                e6.setIcon(Drawe);
                f6.setIcon(Drawf);
                g6.setIcon(Drawg);
                h6.setIcon(Drawh);
            }
            case 6 -> {
                a7.setIcon(Drawa);
                b7.setIcon(Drawb);
                c7.setIcon(Drawc);
                d7.setIcon(Drawd);
                e7.setIcon(Drawe);
                f7.setIcon(Drawf);
                g7.setIcon(Drawg);
                h7.setIcon(Drawh);
            }
            case 7 -> {
                a8.setIcon(Drawa);
                b8.setIcon(Drawb);
                c8.setIcon(Drawc);
                d8.setIcon(Drawd);
                e8.setIcon(Drawe);
                f8.setIcon(Drawf);
                g8.setIcon(Drawg);
                h8.setIcon(Drawh);
            }
            default -> {
            }
        }
    }
    
    private ImageIcon setGambar(char symbol){
        switch (symbol){
            case 'r' -> {
                return BBenteng;
            }
            case 'n' -> {
                return BKuda;
            }
            case 'b' -> {
                return BMentri;
            }
            case 'q' -> {
                return BSter;
            }
            case 'k' -> {
                return BRaja;
            }
            case 'p' -> {
                return BPion;
            }
            case 'R' -> {
                return WBenteng;
            }
            case 'N' -> {
                return WKuda;
            }
            case 'B' -> {
                return WMentri;
            }
            case 'Q' -> {
                return WSter;
            }
            case 'K' -> {
                return WRaja;
            }
            case 'P' -> {
                return WPion;
            }
            case 'x' -> {
                return null;
            }
        }
        return null;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new GUI().setVisible(true);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TimeoutException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Arduino;
    private javax.swing.JButton ButKirimData;
    private javax.swing.JButton ButLoad;
    private javax.swing.JButton ButMulai;
    private javax.swing.JButton ButPause;
    private javax.swing.JButton ButPrev;
    private javax.swing.JButton ButPromote;
    private javax.swing.JButton ButSave;
    private javax.swing.JButton ButSetTime;
    private javax.swing.JButton ButTambahkanEngine;
    private javax.swing.JButton ButTambahkanLevel;
    private javax.swing.JComboBox<String> ComboEngine;
    private javax.swing.JComboBox<String> ComboLevel;
    private javax.swing.JTextField CountKomputer;
    private javax.swing.JTextField CountPemain;
    private javax.swing.JTextField JtextFEN;
    private javax.swing.JPanel Kalimat;
    private javax.swing.JButton Koneksikan;
    private javax.swing.JLabel LabelWarnaKomputer;
    private javax.swing.JLabel LabelWarnaPemain;
    private javax.swing.JLabel LogoUDINUS;
    private javax.swing.JPanel PanManualMode;
    private javax.swing.JPanel PanPilihEngine;
    private javax.swing.JPanel PanPilihLevel;
    private javax.swing.JPanel PanPilihMode;
    private javax.swing.JPanel PanPilihWarna;
    private javax.swing.JScrollPane PanelDebug;
    private javax.swing.JPanel PapanCatur;
    private javax.swing.JLabel Perintah;
    private javax.swing.JPanel Permainan;
    private javax.swing.JRadioButton RadAutomatic;
    private javax.swing.JRadioButton RadBenteng;
    private javax.swing.JRadioButton RadClassic;
    private javax.swing.JRadioButton RadHitam;
    private javax.swing.JRadioButton RadKuda;
    private javax.swing.JRadioButton RadManual;
    private javax.swing.JRadioButton RadMentri;
    private javax.swing.JRadioButton RadPutih;
    private javax.swing.JRadioButton RadSemi;
    private javax.swing.JRadioButton RadSter;
    private javax.swing.JTable TabelPermainan;
    private javax.swing.JTextArea TextDebug;
    private javax.swing.JTextField TextKirim;
    private javax.swing.JTextField TextManual;
    private javax.swing.JTextField TextTerima;
    private javax.swing.JTextField TextTimerTotal;
    private javax.swing.JTextField WaktuKomputer;
    private javax.swing.JTextField WaktuPemain;
    private javax.swing.JLabel a1;
    private javax.swing.JPanel a1p;
    private javax.swing.JLabel a2;
    private javax.swing.JPanel a2p;
    private javax.swing.JLabel a3;
    private javax.swing.JPanel a3p;
    private javax.swing.JLabel a4;
    private javax.swing.JPanel a4p;
    private javax.swing.JLabel a5;
    private javax.swing.JPanel a5p;
    private javax.swing.JLabel a6;
    private javax.swing.JPanel a6p;
    private javax.swing.JLabel a7;
    private javax.swing.JPanel a7p;
    private javax.swing.JLabel a8;
    private javax.swing.JPanel a8p;
    private javax.swing.JLabel b1;
    private javax.swing.JPanel b1p;
    private javax.swing.JLabel b2;
    private javax.swing.JPanel b2p;
    private javax.swing.JLabel b3;
    private javax.swing.JPanel b3p;
    private javax.swing.JLabel b4;
    private javax.swing.JPanel b4p;
    private javax.swing.JLabel b5;
    private javax.swing.JPanel b5p;
    private javax.swing.JLabel b6;
    private javax.swing.JPanel b6p;
    private javax.swing.JLabel b7;
    private javax.swing.JPanel b7p;
    private javax.swing.JLabel b8;
    private javax.swing.JPanel b8p;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JLabel c1;
    private javax.swing.JPanel c1p;
    private javax.swing.JLabel c2;
    private javax.swing.JPanel c2p;
    private javax.swing.JLabel c3;
    private javax.swing.JPanel c3p;
    private javax.swing.JLabel c4;
    private javax.swing.JPanel c4p;
    private javax.swing.JLabel c5;
    private javax.swing.JPanel c5p;
    private javax.swing.JLabel c6;
    private javax.swing.JPanel c6p;
    private javax.swing.JLabel c7;
    private javax.swing.JPanel c7p;
    private javax.swing.JLabel c8;
    private javax.swing.JPanel c8p;
    private javax.swing.JLabel d1;
    private javax.swing.JPanel d1p;
    private javax.swing.JLabel d2;
    private javax.swing.JPanel d2p;
    private javax.swing.JLabel d3;
    private javax.swing.JPanel d3p;
    private javax.swing.JLabel d4;
    private javax.swing.JPanel d4p;
    private javax.swing.JLabel d5;
    private javax.swing.JPanel d5p;
    private javax.swing.JLabel d6;
    private javax.swing.JPanel d6p;
    private javax.swing.JLabel d7;
    private javax.swing.JPanel d7p;
    private javax.swing.JLabel d8;
    private javax.swing.JPanel d8p;
    private javax.swing.JLabel e1;
    private javax.swing.JPanel e1p;
    private javax.swing.JLabel e2;
    private javax.swing.JPanel e2p;
    private javax.swing.JLabel e3;
    private javax.swing.JPanel e3p;
    private javax.swing.JLabel e4;
    private javax.swing.JPanel e4p;
    private javax.swing.JLabel e5;
    private javax.swing.JPanel e5p;
    private javax.swing.JLabel e6;
    private javax.swing.JPanel e6p;
    private javax.swing.JLabel e7;
    private javax.swing.JPanel e7p;
    private javax.swing.JLabel e8;
    private javax.swing.JPanel e8p;
    private javax.swing.JLabel f1;
    private javax.swing.JPanel f1p;
    private javax.swing.JLabel f2;
    private javax.swing.JPanel f2p;
    private javax.swing.JLabel f3;
    private javax.swing.JPanel f3p;
    private javax.swing.JLabel f4;
    private javax.swing.JPanel f4p;
    private javax.swing.JLabel f5;
    private javax.swing.JPanel f5p;
    private javax.swing.JLabel f6;
    private javax.swing.JPanel f6p;
    private javax.swing.JLabel f7;
    private javax.swing.JPanel f7p;
    private javax.swing.JLabel f8;
    private javax.swing.JPanel f8p;
    private javax.swing.JLabel g1;
    private javax.swing.JPanel g1p;
    private javax.swing.JLabel g2;
    private javax.swing.JPanel g2p;
    private javax.swing.JLabel g3;
    private javax.swing.JPanel g3p;
    private javax.swing.JLabel g4;
    private javax.swing.JPanel g4p;
    private javax.swing.JLabel g5;
    private javax.swing.JPanel g5p;
    private javax.swing.JLabel g6;
    private javax.swing.JPanel g6p;
    private javax.swing.JLabel g7;
    private javax.swing.JPanel g7p;
    private javax.swing.JLabel g8;
    private javax.swing.JPanel g8p;
    private javax.swing.JLabel h1;
    private javax.swing.JPanel h1p;
    private javax.swing.JLabel h2;
    private javax.swing.JPanel h2p;
    private javax.swing.JLabel h3;
    private javax.swing.JPanel h3p;
    private javax.swing.JLabel h4;
    private javax.swing.JPanel h4p;
    private javax.swing.JLabel h5;
    private javax.swing.JPanel h5p;
    private javax.swing.JLabel h6;
    private javax.swing.JPanel h6p;
    private javax.swing.JLabel h7;
    private javax.swing.JPanel h7p;
    private javax.swing.JLabel h8;
    private javax.swing.JPanel h8p;
    private javax.swing.JComboBox<String> jComboCOM;
    private javax.swing.JButton jFENUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jStartFEN;
    // End of variables declaration//GEN-END:variables

}
