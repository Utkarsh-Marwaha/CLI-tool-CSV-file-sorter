import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class CSVFile {
// /students/u6146791/Desktop/input-data (1).csv

    /**
     * I'm treating each individual row (record) as a string and the overall CSV
     * file is stored as a list of multiple records i.e an a List of (array of strings)
     */
    private List<String[]> records;

    //constants for sortDirection
    final public int SortASC = 1;
    final public int SortDESC = -1;
    public String[] colNames;
    private int sortDirection = SortASC; //1 for ASC, -1 for DESC
    String fileName;

    /**
     * The total number of columns in the CSV file
     */
    private int colsCount=0;

    /**
     *
     * @param filePath path to the csv file which is to be parsed
     * @throws IOException
     */
    public CSVFile(String filePath) throws IOException{

        // instantiate the list of (array of strings) which would hold all the records of the csv file
        records = new ArrayList<>();
        fileName = filePath;
        try(BufferedReader in = new BufferedReader(new FileReader(filePath))) {
            String ln;
            while( (ln = in.readLine()) !=null) {
                colsCount = ln.split(",").length; //FIXME
                records.add(ln.split(","));
            }

            /*
             * This removes the row containing the column names so that we don't sort them accidentally
             */
            if (records.size() >= 1){
                colNames = records.get(0);
                records.remove(0);
            }
        }

    }

    /**
     * method to print all the records of the CSV file
     */
    public void print(){
        for(String[] arr : records){
            for (String s:arr) {
                System.out.print(s+"\t");
            }
            System.out.println();
        }
    }

    /**
     * method to write the records to the same csv file
     * @throws IOException
     */
    public void save() throws IOException{
        try(BufferedWriter out = new BufferedWriter(new FileWriter("/students/u6146791/Desktop/1.csv"))) {
            if (colNames != null){
                records.add(0,colNames);
            }
            for(String[] arr : records){
                for (String s:arr) {
                    out.write(s+"\t");
                }
                out.write("\n");
            }
        }
    }

    /**
     * this method just lets us set the desired sorting direction
     * @param direction the direction in which we want to sort the records of the csv file
     *                  direction could be ascending or descending
     */
    public void setSortDirection(int direction){
        sortDirection = direction;
    }

    /**
     *
     * @param colIndex index of the column based on which we need to sort the records of the csv file
     */
    public void sortByCol(final int colIndex){
        //comparator by specific col
        Comparator<String[]> comp = new Comparator<String[]>(){
            public int compare(String[] a, String[] b){

                if (!(a[colIndex].length()==0 || b[colIndex].length()==0)) {

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yy");
                    try {
                        sdf.parse(a[colIndex]);
                    } catch (ParseException e) {

                        String reg_Integer = "^\\d+$";
                        String reg_Float = "[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)";

                        boolean a_is_Integer = a[colIndex].matches(reg_Integer);
                        boolean b_is_Integer = b[colIndex].matches(reg_Integer);

                        boolean a_is_Float = a[colIndex].matches(reg_Float);
                        boolean b_is_Float = b[colIndex].matches(reg_Float);

                        boolean isInteger = a_is_Integer && b_is_Integer;

                        if (!isInteger) {

                            boolean isFloat = (a_is_Integer && b_is_Float) ||
                                    (a_is_Float && b_is_Integer) || (a_is_Float && b_is_Float);

                            if (!isFloat) {
                                // not an integer and not a float but a string
                                return sortDirection * a[colIndex].compareTo(b[colIndex]);
                            } else {
                                // not an integer but a float
                                return sortDirection * Float.valueOf(a[colIndex]).compareTo(Float.valueOf(b[colIndex]));
                            }

                        } else {
                            // not a float and not a string but an integer
                            return sortDirection * Integer.valueOf(a[colIndex]).compareTo(Integer.valueOf(b[colIndex]));
                        }
                    }

                    // After reaching here, we are sure that the two strings are of type Date
                    // but the compiler doesn't trust us therefore we enclose the parse operation in a try-catch block
                    try {
                        Date date1 = sdf.parse(a[colIndex]);
                        Date date2 = sdf.parse(b[colIndex]);
                        return sortDirection * date1.compareTo(date2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

                if (a[colIndex].length()==0){
                    return 1;
                } else if (b[colIndex].length()==0){
                    return -1;
                }

                return 0;

            }
        };

        Collections.sort(records, comp);
    }

    public int getColsCount(){
        return colsCount;
    }
}



class SortCSV {
    private static BufferedReader in;

    private static String fileName;

    public static void main(String args[]) throws IOException{

        in = new BufferedReader(new InputStreamReader(System.in));
        if (args.length<1){
            //promt user for input file
            System.out.println("Enter path to .CSV file: ");
            fileName = in.readLine();
        } else
        {
            fileName = args[0];
        }

        try {
            CSVFile csv = new CSVFile(fileName);


            csv.print();

            int maxcol = csv.getColsCount();
            System.out.println(
                    String.format("Select sorting column (1-%d): [1] ",maxcol));

            int sortCol = 1;
            String res =in.readLine();

            //if non-default
            if (res.trim().length() != 0){
                sortCol = Integer.parseInt(res);
                if ((sortCol<1) || (sortCol >maxcol)){
                    System.out.println("Incorrect column number");
                    System.exit(0);
                }
            }

            String sortDirection = "1";
            System.out.println("Select sort direction");
            System.out.println("1. ASC");
            System.out.println("2. DESC");
            System.out.print("[1]:");
            sortDirection = in.readLine();
            if (sortDirection == "2")
                csv.setSortDirection(csv.SortDESC);

            csv.sortByCol(sortCol-1); //-1 map from 1:n to 0:n-1
            csv.save();
            System.out.println("Sorted and saved to file");
            System.out.println("Would you like to see result before exit? (yes/no)[no]");
            if (in.readLine().trim().equals("yes"))
                csv.print();
        } catch(IOException e) {
            System.out.println("File doesn't exist");
            System.exit(0);
        }
    }
}