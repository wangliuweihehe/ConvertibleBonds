package com.wlw.stock.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.wlw.stock.R;
import com.wlw.stock.model.MainDataBean;

import org.jetbrains.annotations.NotNull;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;

public class MainListAdapter extends BaseQuickAdapter<MainDataBean, BaseViewHolder> {
    private List<String> codes;

    public MainListAdapter() {
        super(R.layout.item_main);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, MainDataBean item) {
        baseViewHolder
                .setText(R.id.tv_name, item.getName())
                .setText(R.id.tv_code, item.getCode() + " 复制")
                .setText(R.id.tv_price, item.getPrice())
                .setText(R.id.tv_equity, item.getEquity())

                .setText(R.id.tv_date, item.getProgressDate())
                .setText(R.id.tv_number, item.getNumber())
        ;
// .setText(R.id.tv_progress, item.getProgress())

        HtmlTextView htmlTextView = baseViewHolder.getView(R.id.tv_progress);

        htmlTextView.setHtml(item.getProgress());

        TextView tvCode = baseViewHolder.getView(R.id.tv_code);
        if (codes == null || codes.size() == 0) {
            tvCode.setTextColor(Color.parseColor("#34A8F3"));
        } else {
            if (codes.contains(item.getCode())) {
                tvCode.setTextColor(Color.parseColor("#FF3F46"));
            } else {
                tvCode.setTextColor(Color.parseColor("#34A8F3"));
            }
        }
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
        notifyDataSetChanged();
    }
}
