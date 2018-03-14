package ru.news.service.Impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import org.springframework.stereotype.Service;
import ru.news.comparator.JournalArticleDTOComparator;
import ru.news.mapper.JournalArticleMap;
import ru.news.model.JournalArticleDTO;
import ru.news.service.JournalArticleCustomService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class JournalArticleCustomServiceImpl implements JournalArticleCustomService {

    @Override
    public List<JournalArticleDTO> getJournalArticlesLatestVersion(Boolean showArchiveNews) {
        if (showArchiveNews) return getApprovedAndExpiredJournalArticlesLatestVersion();
        else return getApprovedJournalArticlesLatestVersion();
    }

    @Override
    public List<JournalArticleDTO> getApprovedJournalArticlesLatestVersion() {
        List<JournalArticleDTO> journalArticleDTOS = JournalArticleMap.toDto(getLatestVersionApprovedJournalArticle());
        journalArticleDTOS.sort(new JournalArticleDTOComparator().reversed());
        return journalArticleDTOS;
    }

    @Override
    public List<JournalArticleDTO> getApprovedAndExpiredJournalArticlesLatestVersion() {
        List<JournalArticleDTO> journalArticleDTOS = JournalArticleMap.toDto(getLatestVersionApprovedAndExpiredJournalArticle());
        journalArticleDTOS.sort(new JournalArticleDTOComparator().reversed());
        return journalArticleDTOS;
    }

    @Override
    public JournalArticleDTO getJournalArticleLatestVersion(long groupId, String articleId) {
        JournalArticle latestVersion = getLatestVersion(groupId, articleId);
        return JournalArticleMap.toDto(latestVersion);
    }

    @Override
    public List<JournalArticleDTO> getJournalArticleByTag(String tag) {
        List<JournalArticleDTO> journalArticleDTOS = getApprovedJournalArticlesLatestVersion();
        List<JournalArticleDTO> articleDTOListWithTag = new ArrayList<>();
        for (JournalArticleDTO journalArticleDTO : journalArticleDTOS) {
            if (journalArticleDTO.getTags().contains(tag)) {
                articleDTOListWithTag.add(journalArticleDTO);
            }
        }
        return articleDTOListWithTag;
    }

    @Override
    public List<JournalArticleDTO> getJournalArticleByCategory(String category) {
        List<JournalArticleDTO> journalArticleDTOS = getApprovedJournalArticlesLatestVersion();
        List<JournalArticleDTO> articleDTOListWithCategory = new ArrayList<>();
        for (JournalArticleDTO journalArticleDTO : journalArticleDTOS) {
            if (journalArticleDTO.getCategory().contains(category)) {
                articleDTOListWithCategory.add(journalArticleDTO);
            }
        }
        return articleDTOListWithCategory;
    }

    @Override
    public JournalArticle getLatestVersion(long groupId, String articleId) {
        double latestVersion;
        JournalArticle journalArticle = null;
        try {
            latestVersion = JournalArticleLocalServiceUtil.getLatestVersion(groupId, articleId);
            journalArticle = JournalArticleLocalServiceUtil.getArticle(groupId, articleId, latestVersion);
        } catch (PortalException | SystemException e) {
            e.printStackTrace();
        }
        return journalArticle;
    }

    private List<JournalArticle> getLatestVersionApprovedJournalArticle() {
        HashMap<String, JournalArticle> journalArticleHashMap = new HashMap<>();
        try {
            for (JournalArticle journalArticle : JournalArticleLocalServiceUtil.getArticles()) {
                String articleId = journalArticle.getArticleId();

                if (!journalArticle.isInTrash() && journalArticle.isApproved())
                    if (!journalArticleHashMap.containsKey(articleId)) {
                        journalArticleHashMap.put(articleId, journalArticle);
                    }
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(journalArticleHashMap.values());
    }

    private List<JournalArticle> getLatestVersionApprovedAndExpiredJournalArticle() {
        HashMap<String, JournalArticle> journalArticleHashMap = new HashMap<>();
        try {
            for (JournalArticle journalArticle : JournalArticleLocalServiceUtil.getArticles()) {
                String articleId = journalArticle.getArticleId();

                if (!journalArticle.isInTrash() && (journalArticle.isApproved() || journalArticle.isExpired()))
                    if (!journalArticleHashMap.containsKey(articleId)) {
                        journalArticleHashMap.put(articleId, journalArticle);
                    }
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(journalArticleHashMap.values());
    }

}
