package vn.hunghd.flutter.plugins.imagecropper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.ColorInt;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static android.app.Activity.RESULT_OK;

public class ImageCropperDelegate implements PluginRegistry.ActivityResultListener {
    private final Activity activity;
    private MethodChannel.Result pendingResult;
    private MethodCall methodCall;
    private FileUtils fileUtils;

    public ImageCropperDelegate(Activity activity) {
        this.activity = activity;
        fileUtils = new FileUtils();
    }

    public void startCrop(MethodCall call, MethodChannel.Result result) {
        String sourcePath = call.argument("source_path");
        Integer maxWidth = call.argument("max_width");
        Integer maxHeight = call.argument("max_height");
        Double ratioX = call.argument("ratio_x");
        Double ratioY = call.argument("ratio_y");
        String title = call.argument("toolbar_title");
        Long color = call.argument("toolbar_color");
        boolean hideBottomControls = call.argument("hide_bottom_controls");
        methodCall = call;
        pendingResult = result;

        File outputDir = activity.getCacheDir();
        File outputFile = new File(outputDir, "image_cropper_" + (new Date()).getTime() + ".jpg");

        Uri sourceUri = Uri.fromFile(new File(sourcePath));
        Uri destinationUri = Uri.fromFile(outputFile);
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setHideBottomControls(hideBottomControls);
        if (title != null) {
            options.setToolbarTitle(title);
        }
        if (color != null) {
            int intColor = color.intValue();
            options.setToolbarColor(intColor);
            options.setStatusBarColor(darkenColor(intColor));
        }
        UCrop cropper = UCrop.of(sourceUri, destinationUri).withOptions(options);
        if (maxWidth != null && maxHeight != null) {
            cropper.withMaxResultSize(maxWidth, maxHeight);
        }
        if (ratioX != null && ratioY != null) {
            cropper.withAspectRatio(ratioX.floatValue(), ratioY.floatValue());
        }
        cropper.start(activity);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                if ("cropImage".equals(methodCall.method)) {
                    final Uri resultUri = UCrop.getOutput(data);
                    HashMap<String, String> toReturn = new HashMap<String, String>();
                    int x = data.getIntExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, -1);
                    int y = data.getIntExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, -1);
                    int w = data.getIntExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, -1);
                    int h = data.getIntExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, -1);
                    toReturn.put("x", Integer.toString(x));
                    toReturn.put("y", Integer.toString(y));
                    toReturn.put("w", Integer.toString(w));
                    toReturn.put("h", Integer.toString(h));
                    toReturn.put("x2", Integer.toString(x + w));
                    toReturn.put("y2", Integer.toString(y + h));
                    toReturn.put("filePath", fileUtils.getPathFromUri(activity, resultUri));

                    finishWithSuccess(toReturn);
                }
                return true;
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                finishWithError("crop_error", cropError.getLocalizedMessage(), cropError);
                return true;
            } else {
                pendingResult.success(null);
                clearMethodCallAndResult();
                return true;
            }
        }
        return false;
    }

    private void finishWithSuccess(HashMap cropData) {
        pendingResult.success(cropData);
        clearMethodCallAndResult();
    }

    private void finishWithError(String errorCode, String errorMessage, Throwable throwable) {
        pendingResult.error(errorCode, errorMessage, throwable);
        clearMethodCallAndResult();
    }


    private void clearMethodCallAndResult() {
        methodCall = null;
        pendingResult = null;
    }

    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
