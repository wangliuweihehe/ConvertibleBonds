package com.wlw.stock.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class BaseAccessibilityService extends AccessibilityService {

    private AccessibilityManager mAccessibilityManager;
    private Context mContext;
    private static BaseAccessibilityService mInstance;



    public void init(Context context) {
        mContext = context.getApplicationContext();
        mAccessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public static BaseAccessibilityService getInstance() {
        if (mInstance == null) {
            mInstance = new BaseAccessibilityService();
        }
        return mInstance;
    }

    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    protected boolean checkAccessibilityEnabled(String serviceName) {
        List<AccessibilityServiceInfo> accessibilityServices =
                mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 前往开启辅助服务界面
     */
    public void goAccess() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 模拟返回操作
     */
    public void performBackClick() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    /**
     * 模拟下滑操作
     */
    public void performScrollBackward() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    /**
     * 模拟上滑操作
     */
    public void performScrollForward() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    /**
     * 查找对应文本的View
     *
     * @param text text
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        return findViewByText(text, false);
    }

    /**
     * 查找对应文本的View
     *
     * @param text      text
     * @param clickable 该View是否可以点击
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AccessibilityNodeInfo findViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    public void clickTextViewByText(String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void clickTextViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public void inputText(AccessibilityNodeInfo nodeInfo, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }

    public AccessibilityNodeInfo findWebViewNode() {
        return findWebViewNode(getRootInActiveWindow());
    }

    protected AccessibilityNodeInfo findWebViewNode(AccessibilityNodeInfo rootNode) {

        if (rootNode == null) {
            return null;
        }

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo child = rootNode.getChild(i);
            if (TextUtils.equals(child.getClassName(), "android.webkit.WebView")) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    AccessibilityNodeInfo childChild = child.getChild(j);
                    if (TextUtils.equals(childChild.getClassName(), "android.webkit.WebView")) {
                        return childChild;
                    }
                }
            }

        }
        return null;
    }

    protected AccessibilityNodeInfo getWebViewChildNode(String desc) {

        AccessibilityNodeInfo webViewNode = findWebViewNode();

        if (webViewNode != null) {
            for (int i = 0; i < webViewNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = webViewNode.getChild(i);
                if (TextUtils.equals(desc, child.getText() + "")) {
                    return child;
                }
            }
        }
        return null;
    }

    protected AccessibilityNodeInfo getWebViewChildNode(AccessibilityNodeInfo webViewNode, String desc) {
        if (webViewNode != null) {
            for (int i = 0; i < webViewNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = webViewNode.getChild(i);
                if (TextUtils.equals(desc, child.getContentDescription() + "")) {
                    return child;
                }
            }
        }
        return null;
    }


    /**
     * 设置ListView列表逐行往下滚动（GridView也类似）
     *
     * @param viewId
     */
    protected void setListScrollDown(String viewId) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        List<AccessibilityNodeInfo> infoList = root.findAccessibilityNodeInfosByViewId(viewId);
        if (infoList != null && infoList.size() > 0) {
            AccessibilityNodeInfo nodeInfo = infoList.get(0);
            for (int k = 0; k < nodeInfo.getChildCount(); k++) {
                AccessibilityNodeInfo child = nodeInfo.getChild(k);
                if (child != null) {
                    //逐行滚动。
                    child.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    child.performAction(AccessibilityNodeInfo.ACTION_SELECT);
                }
            }
        }
    }


    /**
     * 设置选中列表指定item并触发点击事件，（GridView也类似）
     * infoList的大小为当前可见item数量，position的值为当前列表item的位置
     *
     * @param viewId
     * @param position
     */
    protected void setSelectedListItem(String viewId, int position) {

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        List<AccessibilityNodeInfo> infoList = root.findAccessibilityNodeInfosByViewId(viewId);

        if (infoList != null && infoList.size() > 0) {
            AccessibilityNodeInfo nodeInfo = infoList.get(0);
            if (nodeInfo != null) {
                int childCount = nodeInfo.getChildCount();
                if (position >= 0 && position <= childCount - 1) {
                    AccessibilityNodeInfo child = nodeInfo.getChild(position);
                    if (child != null) {
                        child.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        child.performAction(AccessibilityNodeInfo.ACTION_SELECT);
                        child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                } else {
                    AccessibilityNodeInfo child = nodeInfo.getChild(0);
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }


    /**
     * 设置ListView滚动到顶部（GridView也类似）
     *
     * @param viewId
     */
    protected void setListScrollTop(String viewId) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        List<AccessibilityNodeInfo> infoList = root.findAccessibilityNodeInfosByViewId(viewId);

        if (infoList != null && infoList.size() > 0) {
            AccessibilityNodeInfo nodeInfo = infoList.get(0);
            if (nodeInfo != null) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
