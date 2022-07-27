package com.document.pdfscanner.adapter;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import com.bumptech.glide.Glide;
import com.document.pdfscanner.R;
import com.document.pdfscanner.ulti.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrganizeMergePDFAdapter extends RecyclerView.Adapter<OrganizeMergePDFAdapter.OrganizePagesViewHolder> {

    static String THUMBNAILS_DIR;
    public final String TAG = OrganizeMergePDFAdapter.class.getSimpleName();
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context mContext;
    private List<File> mergePDFs;
    private SparseBooleanArray selectedPages = new SparseBooleanArray();

    private class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            this.view = ((Activity) OrganizeMergePDFAdapter.this.mContext).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            colorFrom = mContext.getResources().getColor(R.color.ui_red);
            colorTo = mContext.getResources().getColor(R.color.ui_white);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.activity_organize_pages_action_mode, menu);
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    this.view.setSystemUiVisibility(this.flags);
                }
                @SuppressLint("RestrictedApi") ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                ofObject.setDuration(300);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) OrganizeMergePDFAdapter.this.mContext).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                OrganizeMergePDFAdapter organizeMergePDFAdapter = OrganizeMergePDFAdapter.this;
                organizeMergePDFAdapter.removeItems(organizeMergePDFAdapter.getSelectedPages());
                actionMode.finish();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            OrganizeMergePDFAdapter.this.clearSelection();
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    flags &= -8193;
                    this.view.setSystemUiVisibility(this.flags);
                }
                @SuppressLint("RestrictedApi") ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), this.colorTo, this.colorFrom);
                ofObject.setDuration(300);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) mContext).getWindow().setStatusBarColor(mContext.getResources().getColor(R.color.ui_red)));
                ofObject.start();
            }
            OrganizeMergePDFAdapter.this.actionMode = null;
        }
    }

    public class OrganizePagesViewHolder extends RecyclerView.ViewHolder {
        public TextView fileName;
        LinearLayout highlightSelectedItem;
        public RelativeLayout pdfWrapper;
        ImageView thumbnail;

        public OrganizePagesViewHolder(View view) {
            super(view);
            this.pdfWrapper = view.findViewById(R.id.pdf_wrapper);
            this.fileName = view.findViewById(R.id.file_name);
            this.thumbnail = view.findViewById(R.id.pdf_thumbnail);
            this.highlightSelectedItem = view.findViewById(R.id.highlight_selected_item);
        }
    }

    public OrganizeMergePDFAdapter(Context context, List<File> list) {
        this.mergePDFs = list;
        this.mContext = context;
        this.actionModeCallback = new ActionModeCallback();
        StringBuilder sb = new StringBuilder();
        sb.append(context.getCacheDir());
        sb.append("/Thumbnails/");
        THUMBNAILS_DIR = sb.toString();
        String str = this.TAG;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("number of thumbs ");
        sb2.append(list.size());
        Log.d(str, sb2.toString());
    }

    public OrganizePagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new OrganizePagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pdfs_merge_grid, viewGroup, false));
    }

    public void onBindViewHolder(final OrganizePagesViewHolder organizePagesViewHolder, int i) {

        File file = this.mergePDFs.get(i);
        StringBuilder sb = new StringBuilder();
        sb.append(THUMBNAILS_DIR);
        sb.append(Utils.removeExtension(file.getName()));
        sb.append(".jpg");
        Glide.with(this.mContext).load(Utils.getImageUriFromPath(sb.toString())).into(organizePagesViewHolder.thumbnail);
        organizePagesViewHolder.fileName.setText(file.getName());
        toggleSelectionBackground(organizePagesViewHolder, i);
        organizePagesViewHolder.pdfWrapper.setOnClickListener(view -> {
            int adapterPosition = organizePagesViewHolder.getAdapterPosition();
            if (OrganizeMergePDFAdapter.this.actionMode == null) {
                OrganizeMergePDFAdapter organizeMergePDFAdapter = OrganizeMergePDFAdapter.this;
                organizeMergePDFAdapter.actionMode = ((AppCompatActivity) organizeMergePDFAdapter.mContext).startSupportActionMode(actionModeCallback);

            }
            OrganizeMergePDFAdapter.this.toggleSelection(adapterPosition);
            String access$600 = OrganizeMergePDFAdapter.this.TAG;
            StringBuilder sb1 = new StringBuilder();
            sb1.append("Clicked position ");
            sb1.append(adapterPosition);
            Log.d(access$600, sb1.toString());
        });
    }

    public List<File> getPDFsToMerge() {
        return this.mergePDFs;
    }

    public void toggleSelection(int i) {
        if (this.selectedPages.get(i, false)) {
            this.selectedPages.delete(i);
        } else {
            this.selectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int size = this.selectedPages.size();
        if (size == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        StringBuilder sb = new StringBuilder();
        sb.append(size);
        sb.append(" ");
        sb.append(this.mContext.getString(R.string.selected));
        actionMode2.setTitle(sb.toString());
        this.actionMode.invalidate();
    }

    private void toggleSelectionBackground(OrganizePagesViewHolder organizePagesViewHolder, int i) {
        if (isSelected(i)) {
            organizePagesViewHolder.highlightSelectedItem.setVisibility(View.VISIBLE);
        } else {
            organizePagesViewHolder.highlightSelectedItem.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getSelectedPages().contains(Integer.valueOf(i));
    }

    public int getItemCount() {
        return this.mergePDFs.size();
    }

    public void clearSelection() {
        List<Integer> selectedPages2 = getSelectedPages();
        this.selectedPages.clear();
        for (Integer intValue : selectedPages2) {
            notifyItemChanged(intValue);
        }
    }

    public List<Integer> getSelectedPages() {
        int size = this.selectedPages.size();
        List arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(this.selectedPages.keyAt(i));
        }
        return arrayList;
    }

    private void removeItem(int i) {
        this.mergePDFs.remove(i);
        notifyItemRemoved(i);
    }

    public void removeItems(List<Integer> list) {
        Collections.sort(list, (num, num2) -> num2 - num);
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                removeItem(list.get(0));
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(list.get(i - 1) - 1)) {
                    i++;
                }
                if (i == 1) {
                    removeItem(list.get(0));
                } else {
                    removeRange(list.get(i - 1), i);
                }
                if (i > 0) {
                    list.subList(0, i).clear();
                }
            }
        }
    }

    private void removeRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.mergePDFs.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    public void finishActionMode() {
        ActionMode actionMode2 = this.actionMode;
        if (actionMode2 != null) {
            actionMode2.finish();
        }
    }
}

