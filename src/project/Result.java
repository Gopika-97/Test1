package project;

import java.awt.Color;
import static java.awt.Color.red;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;

public class Result extends javax.swing.JFrame {
    
    private ArrayList<String> methodNames;
    private ArrayList<String> functions;
    private ArrayList<String> functionNames;
    private ArrayList<String> recursiveFunctions;
    int thectc ;
    int thecnc;
    private ArrayList<Integer> tot;
    StatementSet ss = new StatementSet();
    public Result() {
        initComponents();
        methodNames = new ArrayList<>();
        functionNames = new ArrayList<>();
        functions = new ArrayList<>();
        recursiveFunctions = new ArrayList<>();
        
        codeTable.setRowHeight(30);
        codeTable.setShowGrid(true);
        codeTable.setBackground(Color.WHITE);
        codeTable.setForeground(Color.BLACK);
    }
   
    private static Pattern ptn = 
            Pattern.compile("(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");
     
    public static ArrayList<String> captureFunctions(String largeText){
        Matcher mtch = ptn.matcher(largeText);
        ArrayList<String> ips = new ArrayList<String>();
        while(mtch.find()){
            ips.add(mtch.group());
        }
        return ips;
    }
    
    private Pattern methodNamePtn = Pattern.compile("[a-zA-Z][a-zA-Z0-9]+\\(");
     
    public String captureMethodsNames(String largeText){
        Matcher mtch = methodNamePtn.matcher(largeText);
        String ips = "";
        while(mtch.find()){
            ips = mtch.group();
        }
        return ips;
    }
    
    public void tableGenrator(String sourceCode){
        
        recursiveFunctions(sourceCode);
        
        Seperator ref1 = new Seperator();  
        sourceCode = ref1.commentOmmitter(sourceCode);
        sourceCode = ref1.stringOmmitter(sourceCode);
        DefaultTableModel model = (DefaultTableModel)codeTable.getModel();
        String appender = "";
        boolean fors = false;
        boolean recursive = false;
        String recursiveMethodName = "";
        LinkedList scope = new LinkedList();
        
        int cp = 0;
        int cr = 0;
        int count = 0;
        
       String lines[] = sourceCode.split("[{;]+");
        for (String l:lines){
            ss.append(l);
        }
        
        Statement st = new Statement(appender);
        ss.displayList();
        
        for(int i = 0; i < sourceCode.length(); i++){
            char position = sourceCode.charAt(i);
            appender = appender + position;  
            
            //recursive functions
            for(String recursiveFunction : recursiveFunctions){
                if(appender.contains(recursiveFunction)&& recursiveFunction.contains(captureMethodsNames(appender))){
                    recursive = true;
                    recursiveMethodName = recursiveFunction;
                }
            }
          Iterator hmIterator2 = ss.StateCnc.entrySet().iterator(); 
                    while (hmIterator2.hasNext()) { 
                        Map.Entry mapElement = (Map.Entry)hmIterator2.next(); 
                        String stri = (String) mapElement.getKey();
                        int val =(int) mapElement.getValue();
                        if(st.searchPattern(appender,stri)!=0){
                            thecnc = val;
                        }
        } 
            Iterator hmIterator = ss.StateCtc.entrySet().iterator(); 
                    while (hmIterator.hasNext()) { 
                        Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
                        String stri = (String) mapElement.getKey();
                        int val =(int) mapElement.getValue();
                        if(st.searchPattern(appender,stri)!=0){
                            thectc = val;
                        }
        } 
                    
            if(position == '{' && recursive){
                System.out.println("{ pushed");
                scope.insertFirst(i, position);
            }
            else if(position == '}' && recursive){
                System.out.println("} popped");
                scope.deleteFirst();
            }
            
            if(!scope.isEmpty() && recursive){
                cr = getCps(appender);
                if(position == '}'){
                    cr = getCps(appender) * 2;
                }
            }
            else{
                cr = 0;
                recursive = false;  
            }
            
            
            
            //checking for "for" word
            String checkFor = ref1.functionChecker(sourceCode, i);
            
            if(checkFor.contains("for")){
                fors = true;                
            }
                        
            //position == ';' ||
            if(!fors){   
                
                if(position == '{' || position == ';' || position == '}'){
                    
        
                    
                    model.addRow(new Object[]{appender, csCount(appender), thectc,thecnc, inheritance(appender), getTW(appender), getCps(appender), cr});
                    recursiveMethodName = "";
                    cp = cp + getCps(appender) + cr;
                    appender = "";
                }
            }
            else{
                if(position == '{'){
                     
                    
                    model.addRow(new Object[]{appender, csCount(appender), thectc, thecnc, inheritance(appender), getTW(appender), getCps(appender), cr});
                    cp = cp + getCps(appender);
                    appender = "";
                    fors=false;
                }
            }
        }
        
        txtCp.setText(Integer.toString(cp));
        
    }
    
    public int getTW(String code){
        int tw =0;
        tw = inheritance(code)+thecnc+thectc;      
        return tw;
    }
      
    public int getCps(String code){
        return getTW(code) * csCount(code);
    }

    public int getCr(String code){
        return getCps(code) * 2;
    }
    
    public void recursiveFunctions(String code){
        functionGenerator(code);
        this.methodNames = (ArrayList<String>) captureFunctions(code);
        
        for(String functionName : methodNames){
            System.out.println(functionName);
        }
        
        for(String function : functions){
            String appender = "";
            int i = 0;
            while(function.charAt(i) != '{'){
                appender = appender + function.charAt(i);
                i++;
            }
            functionNames.add(captureMethodsNames(appender));
        }
        
        System.out.println("=======================");
        for(String functionName : functionNames){
            System.out.println(functionName);
        }
        
        for(String function : functions){
            int i = 0;
            i = skipUnWanted(function, i);
            String appender = "";
            for(;i<function.length();i++){
                appender += function.charAt(i);
            }
            
            for(String functionName : functionNames){
                if(appender.contains(functionName)){
                    recursiveFunctions.add(functionName);
                }
            }  
        }
        
        System.out.println("=======================");
        for(String recursiveFunction : recursiveFunctions){
            System.out.println(recursiveFunction);
        }
    }
    
     public void functionGenerator(String sourceCode){
        //Creating new instance of code Refactor
        Seperator codeRefactor = new Seperator();

        //ommitting Strings inside String literals
        sourceCode = codeRefactor.stringOmmitter(sourceCode);

        //ommitting single line and multi line comments
        //regex expression is used to ommit multiline and single line
        sourceCode = codeRefactor.commentOmmitter(sourceCode);   
        
        //Level Retriever for get function scope
        LinkedList scope = new LinkedList();

        //function appender
        String functionAppender = "";

        //starting point
        int i = 0;
        //boolean value for check main and classes
        boolean checker = true;

        //for loop looping through the function
        for (; i < sourceCode.length(); i++) {
            //used to skip the main function and classes
            //becuase all recursions we can find inside a class or main
            String check = functionChecker(sourceCode, i);
            //|| check.contains("main")
            if (check.contains("class")) {
                checker = false;
            }

            if (checker) {
                char posision = sourceCode.charAt(i);

                functionAppender = functionAppender + posision;

                if (posision == '{') {
                    scope.insertFirst(i, posision);
                } else if (posision == '}') {
                    try {
                        scope.deleteFirst();
                        if (scope.isEmpty()) {
                            functions.add(functionAppender.trim());
                            functionAppender = "";
                        }
                    } catch (NullPointerException e) {
//                            e.printStackTrace();
                    }
                }
            }
            else {
                i = skipUnWanted(sourceCode, i);
                checker = true;
            }
        }
            
        for(String f : functions){
            System.out.println(f + "\n----------------------------------");
        }           
    }   
    
    private int skipUnWanted(String sourceCode, int i) {
        while(sourceCode.charAt(i) != '{'){
            i++;
        }    
        return i;
    }
    
    public String functionChecker(String sourceCode, int i){
        String appender="";
        if((i+25) > sourceCode.length()){
            for(int j=i; j < sourceCode.length();j++){
                char pos = sourceCode.charAt(j);
                appender = appender+pos;
            }  
        }
        else{
            for(int j=i; j < i+25;j++){
                char pos = sourceCode.charAt(j);
                appender = appender+pos;
            }
        }
        return appender;
    }
    
        public int csCount(String code){
         //String code = code.getText();           
        int count = 0;
        int count1 = 0;
        
        //Checking for Numerical values
        Pattern pattern = Pattern.compile("\\d+");
        Matcher m = pattern.matcher(code);
                
        while (m.find()) {//Numerical values found
            ++count;
        }
             
        //Checking for Strings between double quotes
        Pattern p3 = Pattern.compile("\"([^\"]*)\"");
        Matcher m3 = p3.matcher(code);
                
        while (m3.find()) {//String between double quotes found
            ++count;
        }
        
        //checking method names
        Pattern p6 = Pattern.compile("(public|private|static|protected|abstract|native|synchronized)+([a-zA-Z0-9<>._?, ]*) +([a-zA-Z0-9_]+) *"
                + "\\([a-zA-Z0-9<>\\[\\]._?, \n]*\\) *([a-zA-Z0-9_ ,\n]*) *\\{" );
        Matcher m6 = p6.matcher(code);
                
        while (m6.find()) {//String between double quotes found
            ++count;
        }
        
        //checking variable names
        Pattern p7 = Pattern.compile("^[a-zA-Z_$][a-zA-Z_$0-9]*$");
        Matcher m7 = p7.matcher(code);
                
        while (m7.find()) {//String between double quotes found
            ++count;
        }
        
        
        
        //Checking for Array Names
        String[] checkBrackets = code.split("]");
        
        for (int i = 0; i < checkBrackets.length; i++) {//Array Name found
            ++count1;
        }
        
        count1 = count1 - 1;
        count = count + count1;
             
        //Checking for keyword and Manipulators
        String[] keywordArray = new String[] {"+", "*", "-", "/", "++", "--", "==", "!=", ">", "<", ">=", "<=", "&&", "||", "!", "|", "^", "~", "<<", ">>", ">>>", "<<<", ",", ".", "::", "+=", "-=", "*=", "/=",
                                             "=", ">>>=", "|=", "&=", "%=", "<<=", ">>=", "^=", "void", "double", "int", "float","long", "String","System", "args",
                                            "printf", "println", "cout", "cin", "if", "for", "while", "do-while", "switch", "case", "endln", "\n", "class",
                                            "new","delete","throws","throw",
                                            //numeric values
                                            "[0]|[1-9][0-9]*",
                                            
                                           
        
        
        };
        
        
        for (int i = 0; i < keywordArray.length; i++) { //Iterate through the keyword array, while checking

            String keywordToSearch = keywordArray[i];
            int countWord = 0;
            String tempStr = new String(code); //Create a new string out of "fileContent" because "TempStr" will be split

            while (tempStr.indexOf(keywordToSearch) >= 0) {//Keyword or operators found
                countWord++;

                //Split the tempStr to avoid searching again on the already searched section
                tempStr = tempStr.substring(tempStr.indexOf(keywordToSearch) + keywordToSearch.length());
            }
            count = count + countWord;
        }
          
//        System.out.println("complexity due to size (Cs): " +count);
//        int columns = 0;
        
//        for(int r = 0; r < columns; r++){
//            System.out.println("COLUMN LABEL");
//            if (r == columns -1){
//                System.out.println("\n");
//            }
//        }
        return count;
    }
        
        public int inheritance(String line){
       //**********Measuring the complexity due to inheritance(Ci)**********
            int total = 2;
            int total1 =0;
//Any Parent class has a complexity of 2 due to Inheritance from the Object, Because (Inheritance = number of ancestors + 1)
            if( line == "{" && line=="}"){
                    total=0;
               }
            //Complexity due to Inheritance of a C++ code
            Pattern p1 = Pattern.compile(" : public");
            Matcher m1 = p1.matcher(line);
            
            Pattern p4 = Pattern.compile(" : protected");
            Matcher m4 = p4.matcher(line);
            
            Pattern p5 = Pattern.compile(" : private");
            Matcher m5 = p5.matcher(line);
            
            
            
           
            while(m1.find()|m4.find()|m5.find()){//C++ child found
                total1 = total + 1;
                //total = total1 + total;
            }
        
            //Complexity due to Inheritance of a Java code
            Pattern p2 = Pattern.compile(" extends ");
            Matcher m2 = p2.matcher(line); 
            
            Pattern p3 = Pattern.compile(" class ");
            Matcher m3 = p3.matcher(line);
            
            
            
            while(m2.find()){//Java child found
                total1 = total + 1;
                //total = total1 + total;
            }
            while(m3.find()){//Java child found
                total1 = total;
                //total = total1 + total;
            }

            //System.out.println("complexity due to inheritance : " + total);   
            
            return total1;
    }
        
        
            
//        complexity comp = new complexity();
//        comp.lblKeywords(str);
//        comp.setVisible(true);
           
        
        
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlResult = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        codeTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        txtCp = new javax.swing.JLabel();
        Header = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(1366, 768));

        pnlResult.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Showcard Gothic", 1, 30)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Code Complexity Measuring Tool");
        pnlResult.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 610, 70));

        jLabel1.setFont(new java.awt.Font("Showcard Gothic", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(240, 240, 240));
        jLabel1.setText("Analyzed Results");
        pnlResult.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 60, -1, -1));

        btnBack.setFont(new java.awt.Font("Stencil", 1, 18)); // NOI18N
        btnBack.setText("Back");
        btnBack.setActionCommand("btnAnalyze");
        btnBack.setPreferredSize(new java.awt.Dimension(150, 50));
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        pnlResult.add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 640, -1, -1));

        codeTable.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        codeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Code", "Cs", "Ctc", "Cnc", "Ci", "TW", "Cps", "Cr"
            }
        ));
        jScrollPane1.setViewportView(codeTable);

        pnlResult.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 100, 1260, 520));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setText("CP Value: ");
        pnlResult.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 630, -1, -1));

        txtCp.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtCp.setText("0");
        pnlResult.add(txtCp, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 630, 100, -1));

        Header.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resources/abAqua.jpg"))); // NOI18N
        Header.setMaximumSize(new java.awt.Dimension(1366, 100));
        Header.setMinimumSize(new java.awt.Dimension(1366, 100));
        Header.setPreferredSize(new java.awt.Dimension(1366, 600));
        pnlResult.add(Header, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 720));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlResult, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
           this.dispose();
    }//GEN-LAST:event_btnBackActionPerformed

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
            java.util.logging.Logger.getLogger(Result.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Result.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Result.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Result.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Result().setVisible(true);
            }
        }
        );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Header;
    private javax.swing.JButton btnBack;
    private javax.swing.JTable codeTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlResult;
    private javax.swing.JLabel txtCp;
    // End of variables declaration//GEN-END:variables
}
