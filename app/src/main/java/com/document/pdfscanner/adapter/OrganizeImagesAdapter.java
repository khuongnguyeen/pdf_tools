package com.document.pdfscanner.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.document.pdfscanner.R;
import com.document.pdfscanner.model.ImagePage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrganizeImagesAdapter extends RecyclerView.Adapter<OrganizeImagesAdapter.OrganizePagesViewHolder> {
    public final String TAG = OrganizeImagesAdapter.class.getSimpleName();
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    private List<ImagePage> imagePages;
    public Context mContext;
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
            this.view = ((Activity) mContext).getWindow().getDecorView();
            this.flags = this.view.getSystemUiVisibility();
            colorFrom = mContext.getResources().getColor(R.color.ui_red);
            colorTo = mContext.getResources().getColor(R.color.ui_white);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.activity_organize_pages_action_mode, menu);
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    this.flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    this.view.setSystemUiVisibility(this.flags);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), this.colorFrom, this.colorTo);
                ofObject.setDuration(300);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) OrganizeImagesAdapter.this.mContext).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                OrganizeImagesAdapter organizeImagesAdapter = OrganizeImagesAdapter.this;
                organizeImagesAdapter.removeItems(organizeImagesAdapter.getSelectedPages());
                actionMode.finish();
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            OrganizeImagesAdapter.this.clearSelection();
            int i = VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    flags &= -8193;
                    this.view.setSystemUiVisibility(this.flags);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), this.colorTo, this.colorFrom);
                ofObject.setDuration(300);
                ofObject.addUpdateListener(valueAnimator -> {
                    ((Activity) mContext).getWindow().setStatusBarColor(mContext.getResources().getColor(R.color.ui_red));
                });
                ofObject.start();
            }
            OrganizeImagesAdapter.this.actionMode = null;
        }
    }

    public class OrganizePagesViewHolder extends RecyclerView.ViewHolder {
        LinearLayout highlightSelectedItem;
        public TextView pageNumber;
        public RelativeLayout pdfWrapper;
        ImageView thumbnail;

        private OrganizePagesViewHolder(View view) {
            super(view);
            this.pdfWrapper = (RelativeLayout) view.findViewById(R.id.pdf_wrapper);
            this.pageNumber = (TextView) view.findViewById(R.id.page_number);
            this.thumbnail = (ImageView) view.findViewById(R.id.pdf_thumbnail);
            this.highlightSelectedItem = (LinearLayout) view.findViewById(R.id.highlight_selected_item);
        }
    }

    public OrganizeImagesAdapter(Context context, List<ImagePage> list) {
        this.imagePages = list;
        this.mContext = context;
        this.actionModeCallback = new ActionModeCallback();
    }

    public OrganizePagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new OrganizePagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final OrganizePagesViewHolder organizePagesViewHolder, int i) {
        ImagePage imagePage = (ImagePage) this.imagePages.get(i);
        Glide.with(this.mContext).load(new File(String.valueOf(imagePage.getImageUri()))).into((ImageView) organizePagesViewHolder.thumbnail);
        organizePagesViewHolder.pageNumber.setText(String.valueOf(imagePage.getPageNumber()));
        toggleSelectionBackground(organizePagesViewHolder, i);
        organizePagesViewHolder.pdfWrapper.setOnClickListener(view -> {
            int adapterPosition = organizePagesViewHolder.getAdapterPosition();
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) mContext).startSupportActionMode(OrganizeImagesAdapter.this.actionModeCallback);
            }
            toggleSelection(adapterPosition);
            String access$700 = OrganizeImagesAdapter.this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Clicked position ");
            sb.append(adapterPosition);
            Log.d(access$700, sb.toString());
        });
    }

    public void toggleSelection(int i) {
        if (this.selectedPages.get(i, false)) {
            this.selectedPages.delete(i);
        } else {
            selectedPages.put(i, true);
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
        actionMode2.setTitle((CharSequence) sb.toString());
        actionMode.invalidate();
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
        return this.imagePages.size();
    }

    private void clearSelection() {
        List<Integer> selectedPages2 = getSelectedPages();
        this.selectedPages.clear();
        for (Integer intValue : selectedPages2) {
            notifyItemChanged(intValue.intValue());
        }
    }

    private List<Integer> getSelectedPages() {
        int size = this.selectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(this.selectedPages.keyAt(i));
        }
        return arrayList;
    }

    private void removeItem(int i) {
        this.imagePages.remove(i);
        notifyItemRemoved(i);
    }

    public void removeItems(List<Integer> list) {
        Collections.sort(list, (num, num2) -> num2.intValue() - num.intValue());
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                removeItem(((Integer) list.get(0)).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && ((Integer) list.get(i)).equals(Integer.valueOf(((Integer) list.get(i - 1)).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    removeItem(((Integer) list.get(0)).intValue());
                } else {
                    removeRange(((Integer) list.get(i - 1)).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removeRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.imagePages.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

}
