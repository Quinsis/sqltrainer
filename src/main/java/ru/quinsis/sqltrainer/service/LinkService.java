package ru.quinsis.sqltrainer.service;

import ru.quinsis.sqltrainer.model.mongodb.OfflineLink;
import ru.quinsis.sqltrainer.model.mongodb.OnlineLink;

import java.util.List;
import java.util.Optional;

public interface LinkService {
    Optional<OnlineLink> getOnlineLinkByCode(String code);
    Optional<OfflineLink> getOfflineLinkByCode(String code);
    Optional<OnlineLink> getOnlineLinkBySchemaId(String schemaId);
    Optional<List<OfflineLink>> getOfflineLinksBySchemaId(String schemaId);
    OnlineLink saveOnlineLink(OnlineLink onlineLink);
    OfflineLink saveOfflineLink(OfflineLink offlineLink);
    void deleteOnlineLink(OnlineLink onlineLink);
    void deleteOfflineLink(OfflineLink offlineLink);
}
