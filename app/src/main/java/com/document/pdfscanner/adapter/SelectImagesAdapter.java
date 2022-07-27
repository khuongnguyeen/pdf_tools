package com.document.pdfscanner.adapter;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;
import com.bumptech.glide.Glide;
import com.document.pdfscanner.R;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectImagesAdapter extends RecyclerView.Adapter<SelectImagesAdapter.SelectPDFViewHolder> {
    private final String TAG = SelectImagesAdapter.class.getSimpleName();
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    private List<Uri> imageUris;
    public Context mContext;
    private OnImageSelectedListener onMultiSelectedImageListener;
    private SparseBooleanArray selectedImages = new SparseBooleanArray();

    private class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            view = ((Activity) mContext).getWindow().getDecorView();
            flags = view.getSystemUiVisibility();
            colorFrom = mContext.getResources().getColor(R.color.ui_red);
            colorTo = mContext.getResources().getColor(R.color.ui_white);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.selected_pdfs, menu);

            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    view.setSystemUiVisibility(this.flags);
                }
                @SuppressLint("RestrictedApi") ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), this.colorFrom, this.colorTo);
                ofObject.setDuration(300);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) mContext).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_select) {
                multiSelectedPDF(selectedImages());
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            SelectImagesAdapter.this.clearSelection();
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    flags &= -8193;
                    this.view.setSystemUiVisibility(this.flags);
                }
                @SuppressLint("RestrictedApi") ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), this.colorTo, colorFrom);
                ofObject.setDuration(300);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) mContext).getWindow().setStatusBarColor(mContext.getResources().getColor(R.color.ui_red)));
                ofObject.start();
            }
            SelectImagesAdapter.this.actionMode = null;
        }
    }

    public interface OnImageSelectedListener {
        void onMultiSelectedPDF(ArrayList<String> arrayList);
    }

    public class SelectPDFViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout highlightSelectedItem;
        public ImageView imageThumbnail;

        public SelectPDFViewHolder(View view) {
            super(view);
            this.imageThumbnail = view.findViewById(R.id.image_thumb);
            this.highlightSelectedItem = view.findViewById(R.id.highlight_selected_item);
        }
    }

    public SelectImagesAdapter(Context context, List<Uri> list) {
        this.imageUris = list;
        this.mContext = context;
        actionModeCallback = new ActionModeCallback();
        Context context2 = this.mContext;
        if (context2 instanceof OnImageSelectedListener) {
            this.onMultiSelectedImageListener = (OnImageSelectedListener) context2;
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.mContext.toString());
        sb.append(" must implement OnImageSelectedListener");
        throw new RuntimeException(sb.toString());
    }

    @NotNull
    public SelectPDFViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SelectPDFViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_select_images_grid, viewGroup, false));
    }

    public void onBindViewHolder(@NotNull final SelectPDFViewHolder selectPDFViewHolder, int i) {
        Uri uri = (Uri) this.imageUris.get(i);
        toggleSelectionBackground(selectPDFViewHolder, i);
        Glide.with(mContext).load(new File(String.valueOf(uri))).error(R.drawable.ic_icon_pdf).centerCrop().into(selectPDFViewHolder.imageThumbnail);
        selectPDFViewHolder.imageThumbnail.setOnClickListener(view -> {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) mContext).startSupportActionMode(actionModeCallback);

            }
            toggleSelection(selectPDFViewHolder.getAdapterPosition());
        });
    }

    public void updateData(List<Uri> list) {
        this.imageUris = list;
        notifyDataSetChanged();
    }

    public void toggleSelection(int i) {
        if (this.selectedImages.get(i, false)) {
            this.selectedImages.delete(i);
        } else {
            this.selectedImages.put(i, true);
        }
        notifyItemChanged(i);
        int selectedItemCount = getSelectedItemCount();
        if (selectedItemCount == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        StringBuilder sb = new StringBuilder();
        sb.append(selectedItemCount);
        sb.append(" ");
        sb.append(this.mContext.getString(R.string.selected));
        actionMode2.setTitle((CharSequence) sb.toString());
        this.actionMode.invalidate();
    }

    private int getSelectedItemCount() {
        return this.selectedImages.size();
    }

    private ArrayList<Integer> getSelectedImages() {
        int size = this.selectedImages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(selectedImages.keyAt(i));
        }
        return arrayList;
    }

    public void clearSelection() {
        List<Integer> selectedImages2 = getSelectedImages();
        this.selectedImages.clear();
        for (Integer intValue : selectedImages2) {
            notifyItemChanged(intValue.intValue());
        }
    }

    private boolean isSelected(int i) {
        return getSelectedImages().contains(Integer.valueOf(i));
    }

    private void toggleSelectionBackground(SelectPDFViewHolder selectPDFViewHolder, int i) {
        if (isSelected(i)) {
            selectPDFViewHolder.highlightSelectedItem.setVisibility(View.VISIBLE);
        } else {
            selectPDFViewHolder.highlightSelectedItem.setVisibility(View.INVISIBLE);
        }
    }

    public ArrayList<String> selectedImages() {
        List<Integer> selectedImages2 = getSelectedImages();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer intValue : selectedImages2) {
            arrayList.add(((Uri) this.imageUris.get(intValue.intValue())).toString());
        }
        return arrayList;
    }

    public int getItemCount() {
        return this.imageUris.size();
    }

    public void multiSelectedPDF(ArrayList<String> arrayList) {
        OnImageSelectedListener onImageSelectedListener = this.onMultiSelectedImageListener;
        if (onImageSelectedListener != null) {
            onImageSelectedListener.onMultiSelectedPDF(arrayList);
        }
    }
}