package com.common.listview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.AppLog;

/**
 * Created by houlijiang on 15/11/23.
 * 
 * 对AbsListDataAdapter的简单封装，简化子类写法
 * 每种类型数据对应一个cell
 */
public class BaseListDataAdapter<T> extends AbsListDataAdapter<T> {

    private static final String TAG = BaseListDataAdapter.class.getSimpleName();

    protected static final int DEFAULT_CELL_TYPE = 0;
    private Class<? extends BaseListCell<T>> mDefaultCellClass;

    public BaseListDataAdapter() {
        super();
    }

    public BaseListDataAdapter(IOnLoadMore listener) {
        super(listener);
    }

    /**
     * 如果列表只有一种cell，则构造时直接指定，否则需要重载bindCellType
     * 
     * @param defaultClass 默认cell
     */
    public BaseListDataAdapter(Class<? extends BaseListCell<T>> defaultClass) {
        super();
        mDefaultCellClass = defaultClass;
    }

    /**
     * 如果有多中类型则子类需要重载这个方法来绑定view类型，同时返回类型
     * 
     * @return cell实例
     */
    protected BaseListCell<T> createCell(int type) {
        // 根据类型查找对应cell并创建cell实例
        try {
            if (type == DEFAULT_CELL_TYPE && mDefaultCellClass != null) {
                return mDefaultCellClass.newInstance();
            }
        } catch (InstantiationException e) {
            AppLog.e(TAG, "crate cell e:" + e.getLocalizedMessage());
        } catch (Exception e) {
            AppLog.e(TAG, "crate cell e:" + e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * view 被回收
     * 
     * @param cell cell
     */
    protected void onViewRecycled(BaseListCell cell) {
    }

    protected boolean isCellRecyclable() {
        return true;
    }

    @Override
    public int getViewType(int position) {
        return DEFAULT_CELL_TYPE;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup viewGroup, int type) {
        BaseViewHolder holder = null;
        try {
            BaseListCell<T> listCell = createCell(type);
            int layoutId = listCell.getCellViewLayout();
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            if (view == null) {
                return null;
            }
            holder = new BaseViewHolder<>(view, useBinding(), listCell);
            if (!isCellRecyclable()) {
                holder.setIsRecyclable(false);
            }
        } catch (Exception e) {
            AppLog.e(TAG, "get item view holder e:" + e.getLocalizedMessage());
        }
        return holder;
    }

    @Override
    protected void bindData(ViewHolder viewHolder, int position, T data) {
        BaseViewHolder holder = (BaseViewHolder) viewHolder;
        holder.listCell.bindData(viewHolder, data, position);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof BaseViewHolder) {
            onViewRecycled(((BaseViewHolder) holder).getCell());
        }
    }

    @Override
    protected boolean useBinding() {
        return true;
    }

    protected static class BaseViewHolder<T> extends AbsListDataAdapter.ViewHolder {

        protected BaseListCell<T> listCell;

        protected BaseViewHolder(View itemView, boolean useBinding, BaseListCell<T> listCell) {
            super(itemView, useBinding);
            this.listCell = listCell;
        }

        public BaseListCell getCell() {
            return listCell;
        }
    }
}
