package org.grits.toolbox.ms.annotation.sugar;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class TestGlycoVisitorNGlycanInformation
{

    public static void main(String[] args) throws SugarImporterException, GlycoVisitorException
    {
        String t_sequence = "RES\r\n" + "1b:x-dglc-HEX-1:5\r\n" + "2b:x-lgal-HEX-1:5|6:d\r\n" + "3b:x-dglc-HEX-1:5\r\n"
                + "4b:x-dman-HEX-1:5\r\n" + "5b:x-dman-HEX-1:5\r\n" + "6b:x-dglc-HEX-1:5\r\n" + "7s:n-acetyl\r\n"
                + "8b:x-dman-HEX-1:5\r\n" + "9b:x-dglc-HEX-1:5\r\n" + "10b:x-dgal-HEX-1:5\r\n" + "11s:n-acetyl\r\n"
                + "12s:n-acetyl\r\n" + "13s:n-acetyl\r\n" + "LIN\r\n" + "1:1o(-1+1)2d\r\n" + "2:1o(-1+1)3d\r\n"
                + "3:3o(-1+1)4d\r\n" + "4:4o(-1+1)5d\r\n" + "5:4o(-1+1)6d\r\n" + "6:6d(2+1)7n\r\n" + "7:4o(-1+1)8d\r\n"
                + "8:8o(-1+1)9d\r\n" + "9:9o(-1+1)10d\r\n" + "10:9d(2+1)11n\r\n" + "11:3d(2+1)12n\r\n"
                + "12:1d(2+1)13n\r\n";
        t_sequence = "RES\r\n" + "1b:x-dglc-HEX-1:5\r\n" + "2b:x-dglc-HEX-1:5\r\n" + "3b:x-dman-HEX-1:5\r\n"
                + "4b:x-dman-HEX-1:5\r\n" + "5b:x-dglc-HEX-1:5\r\n" + "6b:x-dgal-HEX-1:5\r\n" + "7s:n-acetyl\r\n"
                + "8b:x-dglc-HEX-1:5\r\n" + "9s:n-acetyl\r\n" + "10b:x-dman-HEX-1:5\r\n" + "11s:n-acetyl\r\n"
                + "12b:x-lgal-HEX-1:5|6:d\r\n" + "13s:n-acetyl\r\n" + "LIN\r\n" + "1:1o(-1+1)2d\r\n"
                + "2:2o(-1+1)3d\r\n" + "3:3o(-1+1)4d\r\n" + "4:4o(-1+1)5d\r\n" + "5:5o(-1+1)6d\r\n" + "6:5d(2+1)7n\r\n"
                + "7:3o(-1+1)8d\r\n" + "8:8d(2+1)9n\r\n" + "9:3o(-1+1)10d\r\n" + "10:2d(2+1)11n\r\n"
                + "11:1o(-1+1)12d\r\n" + "12:1d(2+1)13n";
        SugarImporterGlycoCTCondensed t_importer = new SugarImporterGlycoCTCondensed();
        Sugar t_sugar = t_importer.parse(t_sequence);
        GlycoVisitorNGlycanInformation t_visitorInfo = new GlycoVisitorNGlycanInformation();
        t_visitorInfo.start(t_sugar);
        System.out.println(t_visitorInfo.getBisection());
        System.out.println(t_visitorInfo.isNGlycan());
    }

}
