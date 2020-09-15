package ru.skillbranch.skillarticles.ui.profile

import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_profile.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    override val layout: Int = R.layout.fragment_profile
    override val viewModel: ProfileViewModel by viewModels()
    override val binding: ProfileBinding = ProfileBinding()

    override fun setupViews() {

    }

    private fun updateAvatar(url: String) {
        Glide.with(requireContext())
            .load(url)
            .error(R.drawable.ic_avatar)
            .placeholder(R.drawable.ic_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_avatar)
    }

    inner class ProfileBinding : Binding() {

        var avatar by RenderProp("") { updateAvatar(it) }
        var name by RenderProp("") { tv_name.text = it }
        var about by RenderProp("") { tv_about.text = it }
        var rating by RenderProp(0) { tv_rating.text = resources.getString(R.string.rating, it) }
        var respect by RenderProp(0) { tv_respect.text = resources.getString(R.string.respect, it) }

        override fun bind(data: IViewModelState) {
            data as ProfileState
            data.avatar?.let { avatar = it }
            data.name?.let { name = it }
            data.about?.let { about = it }
            rating = data.rating
            respect = data.respect
        }
    }

}
