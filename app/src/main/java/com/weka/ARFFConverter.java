package com.weka;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Created by Anna on 9/26/2015.
 */
public class ARFFConverter extends Activity {

    public static final int ITEM0 = Menu.FIRST;
    public static final int ITEM1 = Menu.FIRST + 1;

    private static final int promptDialog = 1;
    private static final int FileDetailDialog = 2;
    private static final int SaveModelDialog = 3;
    private static final int ChooseFileDialog = 4;

    private static final int NAIVEBAYES = 0;
    private static final int RBFNETWORK = 1;
    private static final int J48 = 2;
    private static final int ADABOOSTM1 = 3;
    private static final int ZEROR = 4;
    private static final int LinearRegression = 5;

    private static final int SVM = 6;

    private List<String> items = null;
    private List<String> paths = null;
    private String rootPath = "/";
    private String curPath = "/";
    private int TypeFile = 0;
    private ListView list;
    private View vv;
    private TabHost tabHost;

    private String TrainfileName, TestfileName, ModelPathName;
    private String classifierString, TestSummaryString = "", TrainSummaryString = "";
    private String TestSummaryresult = "", MatrixString = "", ClassDetailsString = "";

    private Spinner spinner_1, spinner_2;
    private TextView classifier_trainflie, classifier_testfile;
    private TextView model_text, result_text, file_detail, show_state;
    private Button trainfiledetail, testfiledetail, choosetestfile, choosetrainfile;
    private Button Train, Test, LoadModel, SaveModel, SummaryButton,  MatrixButton, ClassDetailsButton;
    private ProgressBar progress;

    private String[] VAR;
    private static final String[]  ALG = { "NAIVEBAYES" ,"RBFNETWORK", "J48", "ADABOOSTM1","ZEROR", "LinearRegression", "SVM"};
    private ArrayAdapter<String> aspn;

    private float time;
    private Timer timer;
    private NumberFormat df = NumberFormat.getInstance();

    private int chooseALG = 0, chooseVar = 0;
    private Instances instancesTrain, instancesTest;
    private Classifier cfs = null;

    private int TypePrompt;
    private boolean isLoadModel = false;
    private boolean running = false;
    private boolean hasFalse = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arff_converter);
    }

    public void chooseFileA_clicked(View view) {
        showDialog(ChooseFileDialog);
    }

    public void chooseFileB_Clicked(View view) {
        showDialog(ChooseFileDialog);
    }

    public void convert_to_ARFF(View view) {

    }

    private Dialog ChooseFileDialog (Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(
                R.layout.choosefile, (ViewGroup)findViewById(R.id.choosefile_layout));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Train File");
        builder.setIcon(R.drawable.weka);
        list = (ListView)textEntryView.findViewById(R.id.list);
        getFileDir("/sdcard/");
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                File file = new File(paths.get(position));
                if (file.isDirectory()) {
                    if (vv != null)
                        vv.setBackgroundColor(Color.WHITE);
                    curPath = paths.get(position);
                    getFileDir(paths.get(position));
                } else {
                    v.setBackgroundColor(Color.GRAY);
                    if (vv != null)
                        vv.setBackgroundColor(Color.WHITE);
                    vv = v;
                    curPath = paths.get(position);
                }
            }
        });
        Button confirmbutton = (Button)textEntryView.findViewById(R.id.buttonfileConfirm);
        Button canclebutton = (Button)textEntryView.findViewById(R.id.buttonfileCancle);
        confirmbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (curPath.endsWith(".arff")&&(TypeFile == 0||TypeFile == 1)) {
                    running = true;
                    dismissDialog(ChooseFileDialog);
                    progress.setVisibility(View.VISIBLE);
                    show_state.setText("  Opening File,Waiting.");
                    if (TypeFile == 0) {
                        TrainfileName = curPath;
                        classifier_trainflie.setText(TrainfileName);
                    }
                    else if (TypeFile == 1) {
                        TestfileName = curPath;
                        classifier_testfile.setText(TestfileName);
                    }
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            Readfile();
                            Message message = Message.obtain();
                            message.what = 1;
                            mHandler.sendMessage(message);
                            running = false;
                        }
                    }){ }.start();
                }else if (curPath.endsWith(".model")&&TypeFile == 2) {
                    running = true;
                    dismissDialog(ChooseFileDialog);
                    ModelPathName = curPath;
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            Readfile();
                            Message message = Message.obtain();
                            message.what = 1;
                            mHandler.sendMessage(message);
                            running = false;
                        }
                    }){ }.start();
                }else {
                    TypePrompt = 0;
                    showDialog(promptDialog);
                }
            }
        });
        canclebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (TrainfileName == null){
                    TypePrompt = 1;
                    showDialog(promptDialog);
                }else
                    dismissDialog(ChooseFileDialog);
            }

        });
        builder.setView(textEntryView);
        return builder.create();
    }

    private void getFileDir(String filePath) {
        items = new ArrayList<String>();
        paths = new ArrayList<String>();
        File f = new File(filePath);
        File[] files = f.listFiles();
        if (!filePath.equals(rootPath)) {
            items.add("b1");
            paths.add(rootPath);
            items.add("b2");
            paths.add(f.getParent());
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(".arff") || file.isDirectory() || file.getName().endsWith(".model")) {
                items.add(file.getName());
                paths.add(file.getPath());
            }
        }
        list.setAdapter(new FileAdapter(this, items, paths));
    }

    public void Readfile() {
        try {
            if (TypeFile == 0) {
                File file = new File(TrainfileName);
                ArffLoader atf = new ArffLoader();
                atf.setFile(file);
                instancesTrain = atf.getDataSet();
                TrainSummaryString = instancesTrain.toSummaryString();
            }else if (TypeFile == 1) {
                File file = new File(TestfileName);
                ArffLoader atf = new ArffLoader();
                atf.setFile(file);
                instancesTest = atf.getDataSet();
                TestSummaryString = instancesTest.toSummaryString();
            }else if (TypeFile == 2) {
                ObjectInputStream ois=new ObjectInputStream(new FileInputStream(ModelPathName));
                cfs = (Classifier)ois.readObject();
                ois.close();
                isLoadModel = true;
            }

        } catch (IOException e) {}
        catch (ClassNotFoundException e) {}
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    progress.setVisibility(View.GONE);
                    show_state.setText("  Open Complete.");
                    if (TypeFile == 0) {
                        InitVar();
                        Initspinner_2();
                    }else if (TypeFile == 2){
                        tabHost.getTabWidget().getChildAt(1).setClickable(true);
                        tabHost.getTabWidget().getChildAt(2).setClickable(false);
                        tabHost.setCurrentTab(1);
                        classifierString = cfs.toString();
                        model_text.setText(classifierString);
                    }
                    break;
                case 2:
                    progress.setVisibility(View.GONE);
                    if (hasFalse) {
                        show_state.setText("Train Field,use time:"+df.format(time)+"s");
                        showDialog(promptDialog);
                        break;

                    }
                    show_state.setText("Train Complete,use time:"+df.format(time)+"s");
                    model_text.setText(classifierString);
                    tabHost.getTabWidget().getChildAt(1).setClickable(true);
                    tabHost.setCurrentTab(1);
                    break;
                case 3:
                    time += 0.01;
                    show_state.setText("  Training,use time:"+df.format(time)+"s");
                    break;
                case 4:
                    time += 0.01;
                    show_state.setText("  Testing,use time:"+df.format(time)+"s");
                    break;
                case 5:
                    progress.setVisibility(View.GONE);
                    if (hasFalse) {
                        show_state.setText("Test Field,use time:"+df.format(time)+"s");
                        showDialog(promptDialog);
                        break;
                    }
                    show_state.setText("Test Complete,use time:"+df.format(time)+"s");
                    result_text.setText(TestSummaryresult);
                    tabHost.getTabWidget().getChildAt(2).setClickable(true);
                    tabHost.setCurrentTab(2);
            }
            super.handleMessage(msg);
        }
    };

    public void InitVar() {
        VAR = new String[instancesTrain.numAttributes()];
        for (int i = 0; i < instancesTrain.numAttributes(); i++)
            VAR[i] = instancesTrain.attribute(i).name();
        chooseVar = instancesTrain.numAttributes() - 1;
    }

    public void Initspinner_2() {
        aspn = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, VAR);
        aspn.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_2.setAdapter(aspn);
        spinner_2.setSelection(chooseVar);
    }
}
