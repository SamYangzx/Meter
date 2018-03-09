package com.android.meter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.meter.http.SocketConstant;
import com.android.meter.http.SocketControl;
import com.android.meter.util.Constant;
import com.android.meter.util.FileUtil;
import com.android.meter.util.LogUtil;
import com.android.meter.util.ToastUtil;
import com.lzy.imagepicker.DataHolder;
import com.lzy.imagepicker.ImageDataSource;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.adapter.ImageFolderAdapter;
import com.lzy.imagepicker.adapter.ImageRecyclerAdapter;
import com.lzy.imagepicker.adapter.ImageRecyclerAdapter.OnImageCheckListener;
import com.lzy.imagepicker.adapter.ImageRecyclerAdapter.OnImageItemClickListener;
import com.lzy.imagepicker.bean.ImageFolder;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.imageloader.GlideImageLoader;
import com.lzy.imagepicker.ui.ImageCropActivity;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.ui.ImagePreviewActivity;
import com.lzy.imagepicker.util.Utils;
import com.lzy.imagepicker.view.FolderPopUpWindow;
import com.lzy.imagepicker.view.GridSpacingItemDecoration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SendCmdActivity extends BaseActivity implements ImageDataSource.OnImagesLoadedListener,
        OnImageItemClickListener, OnImageCheckListener, ImagePicker.OnImageSelectedListener,
        View.OnClickListener {

    private static final String TAG = LogUtil.COMMON_TAG + SendCmdActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;
    public static final String EXTRAS_TAKE_PICKERS = "TAKE";
    public static final String EXTRAS_IMAGES = "IMAGES";
    private String SCAN_FOLDER; //null mean all folder.

    private ImagePicker imagePicker;

    private boolean isOrigin = false;  //是否选中原图
    private View mFooterBar;     //底部栏
    private Button mBtnOk;       //确定按钮
    private Button mDeleteBtn, mSendBtn;
    private View mllDir; //文件夹切换按钮
    private TextView mtvDir; //显示当前文件夹
    private TextView mBtnPre;      //预览按钮
    private ImageFolderAdapter mImageFolderAdapter;    //图片文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow
    private List<ImageFolder> mImageFolders;   //所有的图片文件夹
    //    private List<ImageFolder> mSelectFolders;   //勾选的图片文件夹
    //    private ImageGridAdapter mImageGridAdapter;  //图片九宫格展示的适配器
    private boolean directPhoto = false; // 默认不是直接调取相机
    private RecyclerView mRecyclerView;
    private ImageRecyclerAdapter mRecyclerAdapter;  //具体某个文件夹下的照片的adapter.
    private ImageFolder mImageFolder;
    private ArrayList<ImageItem> mSendImages;
    private ArrayList<ImageItem> mShowImages = new ArrayList<ImageItem>();
    private boolean needToast = false;
    private ProgressBar mSendBar;
    private boolean mCompletedLoad = false;
    private int mFolderIndex = 0;//用于标记当前正在发送哪个文件夹

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        directPhoto = savedInstanceState.getBoolean(EXTRAS_TAKE_PICKERS, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRAS_TAKE_PICKERS, directPhoto);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_cmd);
        LogUtil.d(TAG, "onCreate");

        imagePicker = ImagePicker.getInstance();
        //为防止电脑端来不及处理接收的照片，故此处设置不允许设置多选文件夹
//        imagePicker.setMultiMode(false);
        imagePicker.clear();

//        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        imagePicker.addOnImageSelectedListener(this);

        Intent data = getIntent();
        // 新增可直接拍照
        if (data != null && data.getExtras() != null) {
            directPhoto = data.getBooleanExtra(EXTRAS_TAKE_PICKERS, false); // 默认不是直接打开相机
            if (directPhoto) {
                if (!(checkPermission(Manifest.permission.CAMERA))) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ImageGridActivity.REQUEST_PERMISSION_CAMERA);
                } else {
                    imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE);
                }
            }
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(EXTRAS_IMAGES);
            imagePicker.setSelectedImages(images);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

//        mImageGridAdapter = new ImageGridAdapter(this, null);
        mImageFolderAdapter = new ImageFolderAdapter(this, null);
        mRecyclerAdapter = new ImageRecyclerAdapter(this, null);
        mRecyclerAdapter.setCanPreview(true);
        initView();

        onImageSelected(0, null, false);
//        SCAN_FOLDER = FileUtil.getPicNumberFolder(true);
        SCAN_FOLDER = null;//scan all folder.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new ImageDataSource(this, SCAN_FOLDER, this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
        } else {
            new ImageDataSource(this, SCAN_FOLDER, this);
        }
    }

    private void initView() {
        mDeleteBtn = (Button) findViewById(R.id.delete_btn);
        mDeleteBtn.setOnClickListener(this);
        mSendBtn = (Button) findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(this);
        updateBtn(false);
        mSendBar = (ProgressBar) findViewById(R.id.send_bar);
        if (!mCompletedLoad) {
            mSendBar.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnPre = (TextView) findViewById(R.id.btn_preview);
        mBtnPre.setOnClickListener(this);
        mFooterBar = findViewById(R.id.footer_bar);
        mllDir = findViewById(R.id.ll_dir);
        mllDir.setOnClickListener(this);
        mtvDir = (TextView) findViewById(R.id.tv_dir);
        if (imagePicker.isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPre.setVisibility(View.GONE);
        }
        mRecyclerView.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageDataSource(this, SCAN_FOLDER, this);
            } else {
                ToastUtil.showToast(mContext, "权限被禁止，无法选择本地图片");
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE);
            } else {
                ToastUtil.showToast(mContext, "权限被禁止，无法打开相机");
            }
        }
    }

    @Override
    protected void onStart() {
        SocketControl.getInstance().setListener(this);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        SocketControl.getInstance().setListener(null);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);  //多选不允许裁剪裁剪，返回数据
            finish();
        } else if (id == R.id.ll_dir) {
            if (mImageFolders == null) {
                Log.i("ImageGridActivity", "您的手机没有图片");
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            Intent intent = new Intent(mContext, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getSelectedImages());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            intent.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else if (id == R.id.btn_back) {
            //点击返回按钮
            finish();
        } else if (id == R.id.delete_btn) {
            deleteFolders();
        } else if (id == R.id.send_btn) {
            //TODO Send cmd
            mSendBar.setVisibility(View.VISIBLE);
//            mSendImages = imagePicker.getSelectedImages();
            sendFolderCmd();
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                imagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mRecyclerAdapter.refreshData(imageFolder.images);
                    mtvDir.setText(imageFolder.name);
                }
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        LogUtil.d(TAG, "onImagesLoaded!!");
        mCompletedLoad = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendBar.setVisibility(View.GONE);
            }
        });
        this.mImageFolders = imageFolders;
        imagePicker.setImageFolders(imageFolders);
//        printImageFolders(imageFolders);
        if (imageFolders.size() == 0) {
//            mImageGridAdapter.refreshData(null);
            mRecyclerAdapter.refreshData(null);
        } else {
            setLatestFolder();
//            mImageGridAdapter.refreshData(imageFolders.get(0).images);
//            mRecyclerAdapter.refreshData(mImageFolder.images);
            getShowImages(imageFolders);
            mRecyclerAdapter.refreshData(mShowImages);
        }
//        mImageGridAdapter.setOnImageItemClickListener(this);
        mRecyclerAdapter.setOnImageItemClickListener(this);
        mRecyclerAdapter.setOnImageCheckListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dp2px(this, 2), false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mImageFolderAdapter.refreshData(imageFolders);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private List<ImageItem> getShowImages(List<ImageFolder> imageFolders) {
        if (imageFolders == null) {
            mShowImages.clear();
            return null;
        }
        int size = imageFolders.size();
        if (size == 0) {
            mShowImages.clear();
            return null;
        }
        ArrayList<ImageItem> items = new ArrayList<ImageItem>();
        for (int i = 0; i < size; i++) {
            LogUtil.d(TAG, "getShowImages.i: " + i + " ---: " + imageFolders.get(i).images.get(0).path);
            items.add(imageFolders.get(i).images.get(0));
        }
        mShowImages = items;
        return items;
    }

    private void updateSendImages(int index) {
//        if(mImageFolder)
    }

    /**
     * 照片的预览
     *
     * @param view
     * @param imageItem
     * @param position
     */
    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        //根据是否有相机按钮确定位置
        position = imagePicker.isShowCamera() ? position - 1 : position;
        //针对主界面显示为文件夹的情况, @{
        imagePicker.setCurrentImageFolderPosition(position);
        //@}
        if (imagePicker.isMultiMode()) {
            Intent intent = new Intent(mContext, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);

            /**
             * 2017-03-20
             *
             * 依然采用弱引用进行解决，采用单例加锁方式处理
             */

            // 据说这样会导致大量图片的时候崩溃
//            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getCurrentImageFolderItems());

            // 但采用弱引用会导致预览弱引用直接返回空指针
            DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imagePicker.getCurrentImageFolderItems());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);  //如果是多选，点击图片进入预览界面
        } else {
            imagePicker.clearSelectedImages();
            imagePicker.addSelectedImageItem(position, imagePicker.getCurrentImageFolderItems().get(position), true);
            if (imagePicker.isCrop()) {
                Intent intent = new Intent(mContext, ImageCropActivity.class);
                startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
            } else {
                Intent intent = new Intent();
                intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                finish();
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > 0) {
            mBtnOk.setText(getString(R.string.ip_select_complete, imagePicker.getSelectImageCount(), imagePicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
            mBtnPre.setText(getResources().getString(R.string.ip_preview_count, imagePicker.getSelectImageCount()));
            mBtnPre.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted));
            mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted));
        } else {
            mBtnOk.setText(getString(R.string.ip_complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
            mBtnPre.setText(getResources().getString(R.string.ip_preview));
            mBtnPre.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted));
            mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted));
        }
//        mImageGridAdapter.notifyDataSetChanged();
//        mRecyclerAdapter.notifyItemChanged(position); // 17/4/21 fix the position while click img to preview
//        mRecyclerAdapter.notifyItemChanged(position + (imagePicker.isShowCamera() ? 1 : 0));// 17/4/24  fix the position while click right bottom preview button
        for (int i = imagePicker.isShowCamera() ? 1 : 0; i < mRecyclerAdapter.getItemCount(); i++) {
            if (mRecyclerAdapter.getItem(i).path != null && mRecyclerAdapter.getItem(i).path.equals(item.path)) {
                mRecyclerAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getExtras() != null) {
            if (resultCode == ImagePicker.RESULT_CODE_BACK) {
                isOrigin = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
            } else {
                //从拍照界面返回
                //点击 X , 没有选择照片
                if (data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
                    //什么都不做 直接调起相机
                } else {
                    //说明是从裁剪页面过来的数据，直接返回就可以
                    setResult(ImagePicker.RESULT_CODE_ITEMS, data);
                }
                finish();
            }
        } else {
            //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
            if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
                //发送广播通知图片增加了
                ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());

                /**
                 * 2017-03-21 对机型做旋转处理
                 */
                String path = imagePicker.getTakeImageFile().getAbsolutePath();
//                int degree = BitmapUtil.getBitmapDegree(path);
//                if (degree != 0){
//                    Bitmap bitmap = BitmapUtil.rotateBitmapByDegree(path,degree);
//                    if (bitmap != null){
//                        File file = new File(path);
//                        try {
//                            FileOutputStream bos = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                            bos.flush();
//                            bos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }

                ImageItem imageItem = new ImageItem();
                imageItem.path = path;
                imagePicker.clearSelectedImages();
                imagePicker.addSelectedImageItem(0, imageItem, true);
                if (imagePicker.isCrop()) {
                    Intent intent = new Intent(mContext, ImageCropActivity.class);
                    startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                    setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                    finish();
                }
            } else if (directPhoto) {
                finish();
            }
        }
    }

    /**
     * get latest floder by folder's path.
     */
    private void setLatestFolder() {
        if (mImageFolders != null) {
            String latestFolder = FileUtil.getPicNumberFolder(true);
            for (ImageFolder folder : mImageFolders) {
//                LogUtil.d(TAG, "folder.path: " + folder.path);
                if (latestFolder.equals(folder.path)) {
                    mImageFolder = folder;
                    return;
                }
            }
        }
        if (mImageFolder == null) {
            mImageFolder = new ImageFolder();
        }

    }

    private void printImageFolders(List<ImageFolder> imageFolders) {
        if (imageFolders != null) {
            for (ImageFolder folder : imageFolders) {
                LogUtil.d(TAG, "folder.path: " + folder.path);
            }
        } else {
            LogUtil.d(TAG, "imageFolders is null");

        }
    }

    private void printImage(List<ImageItem> imageItems) {
        if (imageItems != null) {
            for (ImageItem item : imageItems) {
                LogUtil.d(TAG, "printImage.item.path: " + item.path);
            }
        } else {
            LogUtil.d(TAG, "items is null");

        }
    }

    private void deleteFolders() {
        int size = mImageFolders.size();
        List<String> selectedFolders = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            if (mImageFolders.get(i).checked) {
                selectedFolders.add(mImageFolders.get(i).path);
            }
        }
        for (String path : selectedFolders) {
            deleteSelectedFolder(path);
        }
    }

    private void sendFolderCmd() {
        int size = mImageFolders.size();
        for (int i = 0; i < size; i++) {
            ImageFolder folder = mImageFolders.get(i);
            if (folder.checked) {
                LogUtil.d(TAG, "-------send folder: " + folder.path + " begin");
                String parent = FileUtil.getParentFolderPath(folder.path);
                LogUtil.d(TAG, "parent folder: " + parent);
                //发送文件夹下存储的命令
                File cmd = new File(parent, LogUtil.CMD_FILE_NAME);
                try {
                    if (cmd.isFile() && cmd.exists()) { // 判断文件是否存在
                        InputStreamReader read = new InputStreamReader(new FileInputStream(cmd));// 考虑到编码格式
                        BufferedReader bufferedReader = new BufferedReader(read);
                        String lineTxt = null;
                        while ((lineTxt = bufferedReader.readLine()) != null) {
                            LogUtil.d(TAG, "readCMD: " + lineTxt);
                            SocketControl.getInstance().sendMsg(lineTxt, true);
                        }
                        bufferedReader.close();
                        read.close();
                    } else {
                        LogUtil.e(TAG, cmd + " is not exist!!");
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, "read cmd failed.e: " + e);
                }
                //发送文件夹下对应的照片
                mSendImages = folder.images;
                mFolderIndex = i;
                LogUtil.d(TAG, "mFolderIndex: " + mFolderIndex);
                sendChoosePhotos();
                LogUtil.d(TAG, "-------send folder: " + folder.path + " end");
            }
        }

    }

    private void sendChoosePhotos() {
        LogUtil.v(TAG, "sendChoosePhotos.send clickable: " + mSendBtn.isClickable());
        if (mSendImages != null && mSendImages.size() != 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateBtn(false);

                }
            });
            needToast = true;
            List<String> list = new ArrayList<String>();
            int size = mSendImages.size();
            for (int i = 0; i < size; i++) {
                list.add(mSendImages.get(i).path);
            }
            SocketControl.getInstance().sendFiles(list, true);
        }
        printImage(mSendImages);
        printImage(imagePicker.getSelectedImages());
    }


    @Override
    public void onResult(int state, final String data) {
        super.onResult(state, data);
        LogUtil.d(TAG, "state: " + state + " ,data: " + data);
//        if (needToast)
        {
            if (data.startsWith(LogUtil.LOG_PATH)) { //发送的是文件名
                if (SocketConstant.SEND_SUCCESS == state || SocketConstant.COMPUTER_NOT_RESPONSE == state) {
////                    mSendIndex++;
//                    if (mSendIndex >= mSendImages.size()) { //send files completed.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSendBar.setVisibility(View.GONE);
                            ToastUtil.showToast(mContext, "所有照片发送完毕！");
                            updateBtn(true);
                            if (!Constant.TOTAL_DEBUG) {
                                deleteSelectedFolder(FileUtil.getParentFolderPath(FileUtil.getParentFolderPath(data)));
                            }
                        }
                    });
//                        mSendIndex = 0;
//                    } else {
////                        sendChoosePhotos();
//                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSendBar.setVisibility(View.GONE);
                            ToastUtil.showToast(mContext, "发送失败，请检查连接是否正常");
                            updateBtn(true);

                        }
                    });
                }
            } else {//发送的是指令

            }
        }
    }


    private void updateBtn(boolean clickable) {
        updateBtn(mDeleteBtn, clickable);
        updateBtn(mSendBtn, clickable);
    }

    private void updateBtn(Button btn, boolean clickable) {
        if (btn != null) {
            btn.setEnabled(clickable);
            if (clickable) {
                btn.setTextColor(getResources().getColor(R.color.general_textview_color));
            } else {
                btn.setTextColor(getResources().getColor(R.color.general_textview_grey_color));
            }
        }
    }

    @Override
    public void onImageCheck(View view, ImageItem imageItem, int position) {
        LogUtil.v(TAG, "imagePicker.getSelectImageCount: " + imagePicker.getSelectImageCount());
        if (imagePicker.getSelectImageCount() == 0) {
            updateBtn(false);
        } else {
            updateBtn(true);
        }
        boolean checked = mImageFolders.get(position).checked;
        mImageFolders.get(position).checked = !checked;
    }

    private void deleteSelectedFolder(String folder) {
        LogUtil.d(TAG, "deleteSelectedFolder.folder: " + folder);
        //删除对应的文件夹
        FileUtil.deleteDir(folder);
        //删除显示的图片和被选中的图片
        for (Iterator<ImageItem> it = imagePicker.getSelectedImages().iterator(); it.hasNext(); ) {
            ImageItem item = it.next();
            if (item.path.startsWith(folder)) {
                it.remove();
                mShowImages.remove(item);
            }
        }
        mRecyclerAdapter.refreshData(mShowImages);
        //删除对应的文件夹
        for (Iterator<ImageFolder> it = mImageFolders.iterator(); it.hasNext(); ) {
            ImageFolder item = it.next();
            if (item.path.startsWith(folder)) {
                it.remove();
            }
        }
        if (imagePicker.getSelectedImages().size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateBtn(false);
                }
            });
        }
    }


}