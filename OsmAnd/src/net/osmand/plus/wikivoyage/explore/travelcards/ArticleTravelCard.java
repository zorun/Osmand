package net.osmand.plus.wikivoyage.explore.travelcards;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import net.osmand.PicassoUtils;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.widgets.tools.CropCircleTransformation;
import net.osmand.plus.wikipedia.WikiArticleHelper;
import net.osmand.plus.wikivoyage.WikivoyageUtils;
import net.osmand.plus.wikivoyage.article.WikivoyageArticleDialogFragment;
import net.osmand.plus.wikivoyage.data.TravelArticle;
import net.osmand.plus.wikivoyage.data.TravelLocalDataHelper;

public class ArticleTravelCard extends BaseTravelCard {

	public static final int TYPE = 2;

	private TravelArticle article;
	private final Drawable readIcon;
	private FragmentManager fragmentManager;
	private boolean isLastItem;

	public ArticleTravelCard(OsmandApplication app, boolean nightMode, TravelArticle article, FragmentManager fragmentManager) {
		super(app, nightMode);
		this.article = article;
		readIcon = getActiveIcon(R.drawable.ic_action_read_article);
		this.fragmentManager = fragmentManager;
	}

	@Override
	public void bindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
		if (viewHolder instanceof ArticleTravelVH) {
			final ArticleTravelVH holder = (ArticleTravelVH) viewHolder;
			final String url = TravelArticle.getImageUrl(article.getImageTitle(), false);
			Boolean cached = PicassoUtils.isCached(url);

			RequestCreator rc = Picasso.get()
					.load(url);
			WikivoyageUtils.setupNetworkPolicy(app.getSettings(), rc);
			rc.transform(new CropCircleTransformation())
					.into(holder.icon, new Callback() {
						@Override
						public void onSuccess() {
							holder.icon.setVisibility(View.VISIBLE);
							PicassoUtils.setCached(url, true);
						}

						@Override
						public void onError(Exception e) {
							holder.icon.setVisibility(View.GONE);
							PicassoUtils.setCached(url, false);
						}
					});

			holder.icon.setVisibility(cached != null && cached ? View.VISIBLE : View.GONE);
			holder.title.setText(article.getTitle());
			holder.content.setText(WikiArticleHelper.getPartialContent(article.getContent()));
			holder.partOf.setText(article.getGeoDescription());
			holder.leftButton.setText(app.getString(R.string.shared_string_read));
			View.OnClickListener readClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (fragmentManager != null) {
						WikivoyageArticleDialogFragment.showInstance(app, fragmentManager, article.getTripId(), article.getLang());
					}
				}
			};
			holder.leftButton.setOnClickListener(readClickListener);
			holder.itemView.setOnClickListener(readClickListener);
			holder.leftButton.setCompoundDrawablesWithIntrinsicBounds(readIcon, null, null, null);
			updateSaveButton(holder);
			holder.divider.setVisibility(isLastItem ? View.GONE : View.VISIBLE);
			holder.shadow.setVisibility(isLastItem ? View.VISIBLE : View.GONE);
		}
	}

	private void updateSaveButton(final ArticleTravelVH holder) {
		if (article != null) {
			final TravelLocalDataHelper helper = app.getTravelDbHelper().getLocalDataHelper();
			final boolean saved = helper.isArticleSaved(article);
			Drawable icon = getActiveIcon(saved ? R.drawable.ic_action_read_later_fill : R.drawable.ic_action_read_later);
			holder.rightButton.setText(saved ? R.string.shared_string_remove : R.string.shared_string_bookmark);
			holder.rightButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
			holder.rightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (article != null) {
						if (saved) {
							helper.removeArticleFromSaved(article);
						} else {
							app.getTravelDbHelper().createGpxFile(article);
							helper.addArticleToSaved(article);
						}
						updateSaveButton(holder);
					}
				}
			});
		}
	}

	public static class ArticleTravelVH extends RecyclerView.ViewHolder {

		final TextView title;
		final TextView content;
		final TextView partOf;
		final ImageView icon;
		final TextView leftButton;
		final TextView rightButton;
		final View divider;
		final View shadow;

		public ArticleTravelVH(final View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.title);
			content = (TextView) itemView.findViewById(R.id.content);
			partOf = (TextView) itemView.findViewById(R.id.part_of);
			icon = (ImageView) itemView.findViewById(R.id.icon);
			leftButton = (TextView) itemView.findViewById(R.id.left_button);
			rightButton = (TextView) itemView.findViewById(R.id.right_button);
			divider = itemView.findViewById(R.id.divider);
			shadow = itemView.findViewById(R.id.shadow);
		}
	}

	public void setLastItem(boolean lastItem) {
		isLastItem = lastItem;
	}

	@Override
	public int getCardType() {
		return TYPE;
	}
}
