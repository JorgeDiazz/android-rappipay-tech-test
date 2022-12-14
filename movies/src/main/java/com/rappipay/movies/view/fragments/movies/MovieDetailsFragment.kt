package com.rappipay.movies.view.fragments.movies

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.load
import com.app.base.interfaces.Logger
import com.app.base.others.POSTER_IMAGES_BASE_URL
import com.rappipay.components.utils.getCircularProgressImageDrawable
import com.rappipay.components.utils.viewBinding
import com.rappipay.movies.R
import com.rappipay.movies.databinding.FragmentMovieDetailsBinding
import com.rappipay.movies.view.states.MovieDetailsState
import com.rappipay.movies.view.uimodel.MovieUiModel
import com.rappipay.movies.view.uimodel.MovieVideoDataUiModel
import com.rappipay.movies.view.viewmodels.MovieDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Represents the detailed information of a given movie.
 *
 */
@AndroidEntryPoint
class MovieDetailsFragment : DialogFragment(R.layout.fragment_movie_details) {

  @Inject
  lateinit var logger: Logger

  private val viewModel by viewModels<MovieDetailsViewModel>()
  private val binding by viewBinding(FragmentMovieDetailsBinding::bind)

  private val movieUiModel: MovieUiModel by lazy {
    MovieDetailsFragmentArgs.fromBundle(requireArguments()).movieUiModel
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    initializeView()
    initializeObservers()
    initializeNewsSubscription()
  }

  private fun initializeView() {
    initializeToolbar()
    initializeMoviePosterImage()
    initializeMovieTitle()
    initializeMovieData()
    initializeMovieOverview()
    initializeWatchTrailerButton()
  }

  private fun initializeToolbar() {
    binding.toolbarMovieDetails.apply {
      (requireActivity() as AppCompatActivity).setSupportActionBar(this.toolbar)
      setBackButton(true)
      setBackButtonListener { popFragment() }
    }
  }

  private fun initializeMovieTitle() {
    binding.textViewMovieTitle.text = movieUiModel.title
  }

  private fun initializeMovieData() {
    binding.textViewReleaseYear.text = movieUiModel.releaseYear.toString()
    binding.textViewLanguageIsoCode.text = movieUiModel.originalLanguage
    binding.textViewVoteAverage.text = movieUiModel.voteAverage.toString()
  }

  private fun initializeMovieOverview() {
    binding.textViewOverview.text = movieUiModel.overview
  }

  private fun initializeMoviePosterImage() {
    val imageLoader = ImageLoader.Builder(requireContext()).diskCache(null).memoryCache(null).build()

    binding.imageViewMovie.load(POSTER_IMAGES_BASE_URL + movieUiModel.posterPath, imageLoader) {
      crossfade(true)
      error(R.drawable.ic_no_image_found)
      placeholder(getCircularProgressImageDrawable(requireContext()))
    }
  }

  private fun initializeWatchTrailerButton() {
    binding.buttonWatchTrailer.setOnClickListener {
      viewModel.loadMovieVideoData(movieUiModel.id)
    }
  }

  private fun initializeObservers() {
    initializeMovieVideoDataObserver()
  }

  private fun initializeMovieVideoDataObserver() {
    lifecycleScope.launch {
      viewModel.movieVideoDataSharedFlow.flowWithLifecycle(lifecycle).collect { movieVideoDataUiModel ->
        observeMovieVideoData(movieVideoDataUiModel)
      }
    }
  }

  private fun observeMovieVideoData(movieVideoDataUiModel: MovieVideoDataUiModel) {
    movieVideoDataUiModel.videoKey?.let {
      showMovieVideoDialog(it)
    }
  }

  private fun initializeNewsSubscription() {
    lifecycleScope.launch {
      viewModel.newsSharedFlow.flowWithLifecycle(lifecycle).collectLatest { movieDetailStateNews ->
        handleNews(movieDetailStateNews)
      }
    }
  }

  private fun handleNews(news: MovieDetailsState) {
    when (news) {
      is MovieDetailsState.Error -> Toast.makeText(requireContext(), news.errorMessage, Toast.LENGTH_SHORT).show()
    }
  }

  private fun showMovieVideoDialog(videoKey: String) {
    (childFragmentManager.findFragmentByTag(MOVIE_VIDEO_DIALOG_TAG) as? DialogFragment)?.dismiss()
    MovieVideoDialog(videoKey).show(childFragmentManager, MOVIE_VIDEO_DIALOG_TAG)
  }

  private fun popFragment() = requireActivity().onBackPressed()
}
