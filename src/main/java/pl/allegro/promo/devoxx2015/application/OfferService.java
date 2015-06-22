package pl.allegro.promo.devoxx2015.application;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import pl.allegro.promo.devoxx2015.domain.Offer;
import pl.allegro.promo.devoxx2015.domain.OfferRepository;
import pl.allegro.promo.devoxx2015.domain.PhotoScoreSource;

@Component
public class OfferService {
    private static final double MIN_SCORE = 0.7;
    private final OfferRepository offerRepository;
    private final PhotoScoreSource photoScoreSource;

    @Autowired
    public OfferService(OfferRepository offerRepository, PhotoScoreSource photoScoreSource) {
        this.offerRepository = offerRepository;
        this.photoScoreSource = photoScoreSource;
    }

    public void processOffers(List<OfferPublishedEvent> events) {
        for (final OfferPublishedEvent event : events) {
            final double score = score(event.getPhotoUrl());
            if (score >= OfferService.MIN_SCORE) {
                offerRepository.save(new Offer(event.getId(), event.getTitle(), event.getPhotoUrl(), score));
            }
        }
    }

    public List<Offer> getOffers() {
        return offerRepository.findAll(new Sort(Sort.Direction.DESC, "photoScore"));
    }

    private double score(final String url) {
        try {
            return photoScoreSource.getScore(url);
        } catch (Exception ex) {
            return OfferService.MIN_SCORE;
        }
    }
}
