package com.weka;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

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
                        Log.d("toArff", fileA.toString());
                        Log.d("toArff", fileB.toString());
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
                        Log.d("toArffMeanstd", fileA.toString());
                        Log.d("toArffMeanstd", fileB.toString());
                    }
                }
        );
    }

    private File formatFile(String filename){
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root+ "/DCIM/");
        dir.mkdir();
        return new File(dir, filename);
    }
}
