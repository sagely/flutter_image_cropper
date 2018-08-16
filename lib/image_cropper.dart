import 'dart:async';
import 'dart:io';
import 'package:meta/meta.dart';
import 'package:flutter/material.dart';

import 'package:flutter/services.dart';

class ImageCropper {
  static const MethodChannel _channel =
      const MethodChannel('plugins.hunghd.vn/image_cropper');

  /// The actual call to the channel to call the cropImage function.
  static Future <Map> _callCropImage( {
    @required String sourcePath,
    double ratioX,
    double ratioY,
    int maxWidth,
    int maxHeight,
    String toolbarTitle,  // for only Android
    Color toolbarColor,     // for only Android
    bool hideBottomControls, // for only Android
    bool hideRotateControls, // for only iOS
  }) async {
    assert(sourcePath != null);

    if (maxWidth != null && maxWidth < 0) {
      throw new ArgumentError.value(maxWidth, 'maxWidth cannot be negative');
    }

    if (maxHeight != null && maxHeight < 0) {
      throw new ArgumentError.value(maxHeight, 'maxHeight cannot be negative');
    }

    // default hiding bottom controls to true
    if (hideBottomControls == null) {
      hideBottomControls = true;
    }

    if (hideRotateControls == null) {
      hideRotateControls = false;
    }

    return await _channel.invokeMethod(
      'cropImage',
      <String, dynamic> {
        'source_path': sourcePath,
        'max_width': maxWidth,
        'max_height': maxHeight,
        'ratio_x': ratioX,
        'ratio_y': ratioY,
        'toolbar_title': toolbarTitle,
        'toolbar_color': toolbarColor?.value,
        'hide_bottom_controls': hideBottomControls,
        'hide_rotate_controls': hideRotateControls
      }
    );
  }

  /// Loads the image from [sourcePath], represents it on an UI that lets users
  /// crop, rotate the image. If the [ratioX] and [ratioY] are set, it will force
  /// users to crop the image in fixed aspect ratio.
  static Future<File> cropImage({
      @required String sourcePath,
      double ratioX,
      double ratioY,
      int maxWidth,
      int maxHeight,
      String toolbarTitle,  // for only Android
      Color toolbarColor,     // for only Android
      bool hideBottomControls, // for only Android
      bool hideRotateControls, // for only iOS
  }) async {

    final Map cropResults = await _callCropImage(
      sourcePath: sourcePath,
      ratioX: ratioX,
      ratioY: ratioY,
      maxWidth: maxWidth,
      maxHeight: maxHeight,
      toolbarTitle: toolbarTitle,
      toolbarColor: toolbarColor,
      hideBottomControls: hideBottomControls,
      hideRotateControls: hideRotateControls,
    );
    return cropResults != null && cropResults['filePath'] ? new File(cropResults['filePath']) : null;
  }

  /// Loads the image from [sourcePath], represents it on an UI that lets users
  /// crop, rotate the image. If the [ratioX] and [ratioY] are set, it will force
  /// users to crop the image in fixed aspect ratio.  This is the same as the original [cropImage]
  /// function where instead it returns a [Map] which also includes the cropped rectangle.
  static Future<Map> getCropData({
      @required String sourcePath,
      double ratioX,
      double ratioY,
      int maxWidth,
      int maxHeight,
      String toolbarTitle,  // for only Android
      Color toolbarColor,     // for only Android
      bool hideBottomControls, // for only Android
      bool hideRotateControls, // for only iOS
  }) async {
    return await _callCropImage(
      sourcePath: sourcePath,
      ratioX: ratioX,
      ratioY: ratioY,
      maxWidth: maxWidth,
      maxHeight: maxHeight,
      toolbarTitle: toolbarTitle,
      toolbarColor: toolbarColor,
      hideBottomControls: hideBottomControls,
      hideRotateControls: hideRotateControls,
    );
  }
}
