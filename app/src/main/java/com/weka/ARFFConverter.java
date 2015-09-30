package com.weka;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.util.List;

import csvToArffConverter.*;

public class ARFFConverter extends Activity {

    protected EditText labelAFilePath;
    protected EditText labelBFilePath;
    protected Button mConvertToARFF;
    protected Button mConvertToARFFMeanStd;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arff_converter);
        labelAFilePath = (EditText)findViewById(R.id.editTextA);
        labelBFilePath = (EditText)findViewById(R.id.editTextB);
        mConvertToARFF = (Button)findViewById(R.id.convertCSVToARFF);
        mConvertToARFFMeanStd = (Button)findViewById(R.id.convertCSVToARFFWithMeanStdev);

        mConvertToARFF.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v) {
                        String labelA = labelAFilePath.getText().toString();
                        String labelB = labelBFilePath.getText().toString();
                        File fileA = formatFile(labelA);
                        File fileB = formatFile(labelB);
                        List<ARFFData> arffDataList = ConvertCSVToARFF.convert(fileA, fileB);
                        ARFFWriter.convertToARFF(formatFile(getFileName(labelA)+"_train"+".arff"), arffDataList.get(0));
                        ARFFWriter.convertToARFF(formatFile(getFileName(labelB)+"_test"+".arff"), arffDataList.get(1));
                        Log.d("ARFFConverter", "success");
                    }
                }
        );
        mConvertToARFFMeanStd.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v) {
                        String labelA = labelAFilePath.getText().toString();
                        String labelB = labelBFilePath.getText().toString();
                        File fileA = formatFile(labelA);
                        File fileB = formatFile(labelB);
                        List<ARFFData> arffDataList = ConvertCSVToARFF.convertWithStat(fileA, fileB);
                        ARFFWriter.convertToARFF(formatFile(getFileName(labelA)+"_train" + ".arff"), arffDataList.get(0));
                        ARFFWriter.convertToARFF(formatFile(getFileName(labelB)+"_test"+ ".arff"), arffDataList.get(1));
                        Log.d("ARFFConverterMeanStd", "success");
                    }
                }
        );
    }

    private File formatFile(String filename){
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root+ "/Project 2/");
        dir.mkdir();
        return new File(dir, filename);
    }

    private String getFileName (String filename){
        return filename.substring(0, filename.length()-4);
    }
}
