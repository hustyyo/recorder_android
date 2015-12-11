package com.example.mediarecorde;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener, PostTaskInterface {
	
	private String fileName = null;
	private String path = "";
	
	private Button startRecord;
	private Button startPlay;
	private Button stopRecord;
    private Button pauseRecord;
	private Button stopPlay;
	private TextView time;
	private ListView mListView;
	private MAdapter mAdapter;
	private Button delete;
	private Button pausePlay;
    EditText editFrom;
    EditText editTo;

	
	private MediaPlayer mPlayer = null;
	private MediaRecorder mRecorder = null;
	private boolean isPause = false;
	private boolean isPausePlay = false;
	private ArrayList<String> mList = new ArrayList<String>();
	private ArrayList<String> list = new ArrayList<String>();
	private String deleteStr = null;
	private Timer timer;
	private String playFileName = null;
	
	private int second = 0;
	private int minute = 0;
	private int hour = 0;
	private View whichSelecte = null;
	private long limitTime = 0;

    private int maxTime = 30*60*1000;

    CustomProgressDialog progressDialog = null;


    static MainActivity activity = null;

    public static void showFileUploaded(String file){
        String msg = "Uploaded file " + file;
        showToast(msg);
    }

    public static void showToast(final String msg){
        if(null == activity){
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(activity==null){
                    return;
                }
                Toast myToast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
                myToast.setGravity(Gravity.CENTER_VERTICAL, 0, -80);
                myToast.show();
            }
        });

    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        activity  = this;

        ServiceUploadFile.startUploadFile();

		setContentView(R.layout.activity_main);

        progressDialog = new CustomProgressDialog(this);

		initList();
		
		initView();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}


	private void initView() {
		delete = (Button) findViewById(R.id.delete);
		delete.setOnClickListener(this);
		
		delete.setEnabled(false);
		pausePlay = (Button) findViewById(R.id.pausePlay);
		pausePlay.setOnClickListener(this);
		pausePlay.setEnabled(false);
		startRecord = (Button) findViewById(R.id.startRecord);
		startRecord.setOnClickListener(this);
		stopRecord = (Button) findViewById(R.id.stopRecord);
		stopRecord.setOnClickListener(this);
		stopRecord.setEnabled(false);

        pauseRecord = (Button) findViewById(R.id.pauseRecord);
        pauseRecord.setOnClickListener(this);
        pauseRecord.setEnabled(false);

		startPlay = (Button) findViewById(R.id.startPlay);
		startPlay.setOnClickListener(this);
		startPlay.setEnabled(false);
		stopPlay = (Button) findViewById(R.id.stopPlay);
		stopPlay.setOnClickListener(this);
		stopPlay.setEnabled(false);
		time = (TextView) findViewById(R.id.time);
		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new MAdapter(this, list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

        editFrom = (EditText)findViewById(R.id.edit_from);
        editTo = (EditText)findViewById(R.id.edit_to);
	}
    
	private void initList() {
		path = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/collectiveintelligence/";
		
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "SD Card Error！", Toast.LENGTH_LONG).show();
		} else {
			
			File file = new File(path);
			File files[] = file.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().indexOf(".") >= 0) {
						
						
						String fileStr = files[i].getName().substring(
								files[i].getName().indexOf("."));
						if (fileStr.toLowerCase().equals(".mp3")
								|| fileStr.toLowerCase().equals(".amr")
								|| fileStr.toLowerCase().equals(".mp4"))
							list.add(files[i].getName());
					}
				}
			}
		}
	}
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.startRecord:

				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Toast.makeText(this, "SD Card Error, please retry！", Toast.LENGTH_LONG)
					.show();
					break;
				}

				startRecord();
			break;
		case R.id.stopRecord:
                stopRecord(true);
			break;
        case R.id.pauseRecord:
                if (!isPause) {
                    try {
                        pauseRecord();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
		case R.id.startPlay:

			playRecord();
			break;
		case R.id.stopPlay:

			startPlay.setEnabled(true);
			stopPlay.setEnabled(false);
			startRecord.setEnabled(true);
			pausePlay.setEnabled(false);
			if (mPlayer != null) {
				mPlayer.release();
				mPlayer = null;
			}
			delete.setEnabled(true);
			break;
		case R.id.delete:
			deleteRecord();
			break;
		case R.id.pausePlay:
			if (isPausePlay) {
				pausePlay.setText("Pause");
				pausePlay.setEnabled(true);
				isPausePlay = false;
				mPlayer.start();
			} else {
				if (mPlayer != null) {
					mPlayer.pause();
				}
				pausePlay.setText("Resume");
				pausePlay.setEnabled(true);
				isPausePlay = true;
			}
			break;
		default:
			break;
		}
	}

	private boolean limitTime() {
		limitTime = System.currentTimeMillis() - limitTime;
		if (limitTime >= 1100) {
			limitTime = System.currentTimeMillis();
			return true;
		} else {
			return false;
		}
	}

    protected void showDialog(CustomProgressDialog dialog, String message, boolean cancelable){

        if(dialog.isShowing()){
            dialog.setMessage(message);
            return;
        }

        dialog.setCancelable(cancelable);
        dialog.setMessage(message);

        dialog.show();
    }

    protected boolean showProgressDialog(final CustomProgressDialog dialog, final String message, boolean cancelable){

        if(dialog.isShowing()){
            return false;
        }

        showDialog(dialog, message, cancelable);
        return true;
    }
    
	private void deleteRecord() {

		File file = new File(playFileName);
		if (file.exists()) {
			file.delete();
			list.remove(deleteStr);
			mAdapter.notifyDataSetChanged();
			time.setText("");
		} else {
			list.remove(deleteStr);
			mAdapter.notifyDataSetChanged();
		}
		startPlay.setEnabled(false);
		playFileName = null;
		delete.setEnabled(false);
		startRecord.setEnabled(true);
		time.setText("00:00:00");
	}


	private void playRecord() {

		startRecord.setEnabled(false);
		delete.setEnabled(false);
		stopPlay.setEnabled(true);
		startPlay.setEnabled(false);
		pausePlay.setEnabled(true);
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
		mPlayer = new MediaPlayer();

		mPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

                mPlayer.release();
                mPlayer = null;
                startRecord.setEnabled(true);
                startPlay.setEnabled(true);
                stopPlay.setEnabled(false);
                delete.setEnabled(true);
                pausePlay.setEnabled(false);
            }
        });
		try {

			mPlayer.setDataSource(playFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (Exception e) {
			if (mPlayer != null) {
				mPlayer.release();
				mPlayer = null;
			}
			Toast.makeText(this, "Play failed, please retry！", Toast.LENGTH_LONG).show();
			stopPlay.setEnabled(false);
			delete.setEnabled(true);
			pausePlay.setEnabled(false);
		}
	}


	private void stopRecord(boolean checkDuration) {
        if (checkDuration && (System.currentTimeMillis()-limitTime<1100)) {

            Toast.makeText(this, "Recording duration can not be less than 1s！", Toast.LENGTH_SHORT).show();
            return ;
        }


        if (!isPause) {
            try {
                pauseRecord();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

		mRecorder.release();
		mRecorder = null;
		isPause = false;
		startRecord.setEnabled(true);
		startRecord.setText("Start");
		stopRecord.setEnabled(false);
        pauseRecord.setEnabled(false);
		timer.cancel();

        fileName = getFullPath();


		String fileName1 = getTime() + ".amr";
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		FileInputStream fileInputStream = null;
		try {
		    for (int i = 0; i < mList.size(); i++) {
                File file = null;
                try{
                    file = new File(mList.get(i));
                }catch (Exception e){
                    Log.e("ci_recorder","create fail " + e.getMessage());
                }
                if(null == file){
                    continue;
                }

                try{
                    fileInputStream = new FileInputStream(file);
                }catch (Exception e){
                    Log.e("ci_recorder","open fail " + e.getMessage());
                }
                if(null == fileInputStream){
                    continue;
                }

				byte[] mByte = new byte[fileInputStream.available()];
				int length = mByte.length;

				if (i == 0) {
					while (fileInputStream.read(mByte) != -1) {
						fileOutputStream.write(mByte, 0, length);
					}
				}

				else {
					while (fileInputStream.read(mByte) != -1) {

						fileOutputStream.write(mByte, 6, length - 6);
					}
				}
			}

            list.add(fileName1);
            mAdapter.notifyDataSetChanged();

		} catch (Exception e) {

			e.printStackTrace();
            Log.e("ci_recorder", "save fail." + e.getMessage());

//			Toast.makeText(this, "Saving audio file failed！", Toast.LENGTH_LONG).show();
		} finally {
			try {
				fileOutputStream.flush();
				fileInputStream.close();

                sendFile(fileName);

			} catch (Exception e) {
				e.printStackTrace();
			}

			minute = 0;
			hour = 0;
			second = 0;
		}

		for (int i = 0; i < mList.size(); i++) {
			File file = new File(mList.get(i));
			if (file.exists()) {
				file.delete();
			}
		}

	}

    void sendFile(String path){
        currentFile = path;
        showProgressDialog(progressDialog,"Uploading...",true);
        BaseAsyncTask baseAsyncTask = new BaseAsyncTask();
        baseAsyncTask.setPostTask(this);
        baseAsyncTask.execute(path);
    }

	private void pauseRecord() throws InterruptedException {
		if (System.currentTimeMillis()-limitTime<1100) {

			Toast.makeText(this, "Recording duration can not be less than 1s！", Toast.LENGTH_SHORT).show();
			return ;
		}

//		stopRecord.setEnabled(true);
		mRecorder.stop();
		mRecorder.release();
		timer.cancel();
		isPause = true;

		mList.add(fileName);
        pauseRecord.setEnabled(false);
		startRecord.setEnabled(true);
		startRecord.setText("Resume");
//		stopRecord.setText("Stop");
	}

    protected void showAlert(String msg){
        new AlertDialog.Builder(this)
                .setTitle("Sorry!")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    String getFullPath(){
        String fileName = path + "/" + editFrom.getText() + "_" + editTo.getText() + "_" + getTime() + ".amr";
        return fileName;
    }

    boolean checkDuration(){
        int duration = hour*60*60 + minute*60 + second;
        return (duration>=maxTime);
    }
    int getDuration(){
        int duration = hour*60*60 + minute*60 + second;
        return duration;
    }

	private void startRecord() {

        if(editFrom.getText().length()==0 || editTo.getText().length()==0){
            if(editFrom.getText().length()==0){
                showAlert("Please enter the 'Contact Email' field first!");
            } else if(editTo.getText().length()==0){
                showAlert("Please enter the 'Subject Name' field first!");
            }
            return;
        }


		startRecord.setText("Recording...");
		startRecord.setEnabled(false);

		startPlay.setEnabled(false);
		stopRecord.setEnabled(true);
        pauseRecord.setEnabled(true);
		delete.setEnabled(false);
		if (!isPause) {
			mList.clear();
		}
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}

        fileName = getFullPath();

		isPause = false;
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		mRecorder.setOutputFile(fileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        if(checkDuration()){
            stopRecord(false);
            return;
        }

		try {
            mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    stopRecord(false);
                }
            });
            mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
                    {
                        stopRecord(false);
                    }
                }
            });
			mRecorder.prepare();
            recordTime();
		} catch (Exception e) {
			Toast.makeText(this, "Failed to open the recorder！", Toast.LENGTH_LONG).show();
            startPlay.setEnabled(false);
			stopPlay.setEnabled(false);
			delete.setEnabled(false);
			startRecord.setEnabled(false);
			stopRecord.setEnabled(false);
            pauseRecord.setEnabled(false);
			mRecorder.release();
			mRecorder = null;
			this.finish();
		}

		if (mRecorder != null) {
			mRecorder.start();
			limitTime = System.currentTimeMillis();
		}

	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			time.setText(String.format("%1$02d:%2$02d:%3$02d", hour, minute, second));
			super.handleMessage(msg);
		}
	};

	private void recordTime() {
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				second++;
				if (second >= 60) {
					second = 0;
					minute++;
					if (minute >= 60) {
						minute = 0;
						hour++;
					}
				}
				handler.sendEmptyMessage(1);

                if(checkDuration()){
                    stopRecord(false);
                }
			}

		};
		timer = new Timer();
		timer.schedule(timerTask, 1000, 1000);
	}

	private String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date curDate = new Date(System.currentTimeMillis());
		String time = formatter.format(curDate);
		return time;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mRecorder == null) {
			startPlay.setEnabled(true);
			if (mPlayer == null || !mPlayer.isPlaying()) {
				delete.setEnabled(true);
			} else {
				delete.setEnabled(false);
			}
		}
		startPlay.setText("Play");
		if (whichSelecte != null) {
			whichSelecte
					.setBackgroundColor(getResources().getColor(R.color.no));
		}
		view.setBackgroundColor(getResources().getColor(R.color.yes));
		playFileName = path + "/" + list.get(position);
		deleteStr = list.get(position);
		whichSelecte = view;
		time.setText(list.get(position));
	}

	@Override
	protected void onDestroy() {

        activity = null;

		// 删除片段
		if (mList != null && mList.size() > 0) {
			for (int i = 0; i < mList.size(); i++) {
				File file = new File(mList.get(i));
				if (file.exists()) {
					file.delete();
				}
			}
		}
		if (null != mRecorder) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		if (null != mPlayer) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
		if (timer != null) {
			timer.cancel();
		}
		super.onDestroy();
	}

    private void autoPauseRecord() throws InterruptedException {
        mRecorder.stop();
        mRecorder.release();
        timer.cancel();
        isPause = true;

        mList.add(fileName);
        pauseRecord.setEnabled(false);
        startRecord.setEnabled(true);
        startRecord.setText("Resume");
    }

	@Override
	protected void onPause() {
		if (mRecorder != null) {
			try {
                autoPauseRecord();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (mPlayer != null) {
			mPlayer.pause();
			isPausePlay = true;
			pausePlay.setText("Resume Play");
			pausePlay.setEnabled(true);
		}
		super.onPause();
	}

    public void runLaterNoCheck(final Runnable runnable, long delayMillis){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {

                }
            }
        }, delayMillis);
    }

    String currentFile = "";
    static boolean uploadResult = false;
    public static void setUploadResult(boolean d){
        uploadResult = d;
    }

    protected void stopProgressDialog(final CustomProgressDialog dialog, final String message, final long delay, final Runnable runnable){
        if(dialog.isShowing()){
            if(message.length()>0){
                dialog.setMessage(message);
            }
            runLaterNoCheck(new Runnable() {
                @Override
                public void run() {
                    try {
                        dialog.dismiss();
                        if (runnable != null) {
                            runnable.run();
                        }
                    } catch (Exception e) {
                    }
                }
            }, delay);
        }else{
            if(runnable!=null){
                runnable.run();
            }
        }
    }

    @Override
    public void onPostExecute(String file) {
        if(file.equals(currentFile)){
            if(uploadResult){
                if(!progressDialog.isShowing()){
                    String name = file.substring(file.lastIndexOf("/")+1);
                    showFileUploaded(name);
                }else{
                    stopProgressDialog(progressDialog,"Record uploaded.",1000,null);
                }
            }else{
                stopProgressDialog(progressDialog, "Failed to upload the record, we will try again later.",1500,null);
            }
            currentFile = "";
        }
    }
}
