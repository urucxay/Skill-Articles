package ru.skillbranch.skillarticles.ui.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_category_dialog.*
import kotlinx.android.synthetic.main.item_category_dialog.view.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel

class ChoseCategoryDialog : DialogFragment() {

    private val viewModel by activityViewModels<ArticlesViewModel>()
    private val selectedCategories = mutableListOf<String>()
    private val args: ChoseCategoryDialogArgs by navArgs()

    private val categoryAdapter = CategoryAdapter { categoryId, isChecked ->
        if (isChecked) selectedCategories.add(categoryId)
        else selectedCategories.remove(categoryId)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        selectedCategories.clear()
        selectedCategories.addAll(
            savedInstanceState?.getStringArray("checked") ?: args.selectedCategories
        )

        val categories = args.categories.map {
            it.toItem(selectedCategories.contains(it.categoryId))
        }

        categoryAdapter.submitList(categories)

        val listView = layoutInflater.inflate(R.layout.fragment_choose_category_dialog, null) as RecyclerView
        with(listView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Chose category")
            .setPositiveButton("Apply") { _, _ ->
                viewModel.applyCategories(selectedCategories.toList())
            }
            .setNegativeButton("Reset") { _, _ ->
                viewModel.applyCategories(emptyList())
            }
            .setView(listView)

        return builder.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray("checked", selectedCategories.toTypedArray())
        super.onSaveInstanceState(outState)
    }

}

data class CategoryDataItem(
    val categoryId: String,
    val icon: String,
    val title: String,
    val articlesCount: Int = 0,
    val isChecked: Boolean = false
)

fun CategoryData.toItem(isChecked: Boolean = false) = CategoryDataItem(categoryId, icon, title, articlesCount, isChecked)

class CategoryAdapter(private val listener: (String, Boolean) -> Unit) : ListAdapter<CategoryDataItem, CategoryVH>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val containerView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category_dialog, parent, false)
        return CategoryVH(containerView)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        holder.bind(getItem(position), listener)
    }
}


class CategoryVH(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(
        item: CategoryDataItem,
        listener: (String, Boolean) -> Unit
    ) {
        ch_select.setOnCheckedChangeListener(null)

        ch_select.isChecked = item.isChecked
        Glide.with(containerView.context)
            .load(item.icon)
            .apply(RequestOptions.circleCropTransform())
            .override(containerView.iv_icon.width)
            .into(containerView.iv_icon)
        tv_category.text = item.title
        tv_count.text = "${item.articlesCount}"

        ch_select.setOnCheckedChangeListener { _, checked -> listener(item.categoryId, checked) }
        itemView.setOnClickListener { ch_select.toggle() }


    }

}

class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryDataItem>() {

    override fun areItemsTheSame(oldItem: CategoryDataItem, newItem: CategoryDataItem): Boolean =
        oldItem.categoryId == newItem.categoryId

    override fun areContentsTheSame(oldItem: CategoryDataItem, newItem: CategoryDataItem): Boolean =
        oldItem == newItem

}