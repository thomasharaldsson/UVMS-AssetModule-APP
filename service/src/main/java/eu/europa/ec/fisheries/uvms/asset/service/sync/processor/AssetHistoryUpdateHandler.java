package eu.europa.ec.fisheries.uvms.asset.service.sync.processor;

import eu.europa.ec.fisheries.uvms.asset.model.exception.AssetDaoException;
import eu.europa.ec.fisheries.uvms.constant.UnitTonnage;
import eu.europa.ec.fisheries.uvms.dao.AssetRawHistoryDao;
import eu.europa.ec.fisheries.uvms.entity.asset.types.EventCodeEnum;
import eu.europa.ec.fisheries.uvms.entity.asset.types.PublicAidEnum;
import eu.europa.ec.fisheries.uvms.entity.asset.types.SegmentFUP;
import eu.europa.ec.fisheries.uvms.entity.asset.types.TypeOfExportEnum;
import eu.europa.ec.fisheries.uvms.entity.model.AssetEntity;
import eu.europa.ec.fisheries.uvms.entity.model.AssetHistory;
import eu.europa.ec.fisheries.uvms.entity.model.AssetRawHistory;
import eu.europa.ec.fisheries.uvms.entity.model.FishingGear;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class AssetHistoryUpdateHandler {

    @EJB
    private AssetRawHistoryDao assetRawHistoryDao;
    @Inject
    private AssetHistoryRawRecordHandler rawRecordHandler;


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateAssetsHistory(List<String> assetsCfrToUpdate) {
        for (String cfr : assetsCfrToUpdate) {
            updateAssetToFullHistory(cfr);
            log.debug("Asset {} processed for update.", cfr);
        }
    }

    /**
     * Updates and existing asset regarding its history of records
     * @param cfr Asset's CFR
     * @return The same asset object updated
     */
    private AssetEntity updateAssetToFullHistory(String cfr) {
        AssetEntity asset = getAssetByCfr(cfr);
        if (asset == null) {
            return null;
        }
        List<AssetRawHistory> rawRecords = assetRawHistoryDao.getAssetRawHistoryByCfrSortedByEventDate(cfr);
        if (rawRecords.size() > 0) {
            List<AssetRawHistory> newRawRecords = removeFullyDuplicatedRecordsForAsset(rawRecords, asset);
            if (newRawRecords.size() > 0) {
                List<AssetHistory> newRecords = rawRecordHandler.mapRawHistoryToHistory(newRawRecords);
                for(AssetHistory record : newRecords) {
                    record.setActive(false);
                    asset.addHistoryRecord(record);
                    //TODO to be fixed and re-enabled
                    //sendAssetHistoryUpdateToReporting(mapFromAssetHistoryEntity(record, asset.getGuid()));
                }
                AssetHistory mostRecentRecord = getMostRecentHistoryRecordToUpdateAsset(asset, newRecords);
                if (mostRecentRecord != null) {
                    mostRecentRecord.setActive(true);
                    updateAssetFromMostRecentHistoryRecord(asset, mostRecentRecord);
                }
                assetRawHistoryDao.saveAssetWithHistory(asset);
                log.debug("FLEET SYNC: Asset {}. All records {}. Processed {}",
                        cfr, rawRecords.size(), newRecords.size());
            }
        }
        return asset;
    }

    private AssetEntity getAssetByCfr(String assetCfr) {
        try {
            return assetRawHistoryDao.getAssetByCfrWithHistory(assetCfr);
        } catch (AssetDaoException ex) {
            return null;
        }
    }

    private List<AssetRawHistory> removeFullyDuplicatedRecordsForAsset(List<AssetRawHistory> incomingRawRecords,
                                                                       AssetEntity asset) {
        List<AssetHistory> currentRecords = asset.getHistories();
        List<AssetRawHistory> newRawRecords = new ArrayList<>();
        for (AssetRawHistory rawRecord : incomingRawRecords) {
            RawRecordStatus rawRecordStatus = getCurrentRawRecordStatus(currentRecords, rawRecord);
            if (rawRecordStatus.isNew) {
                newRawRecords.add(rawRecord);
            } else if (rawRecordStatus.isUpdate) {
                newRawRecords.add(rawRecord);
                asset.removeHistoryRecord(rawRecordStatus.recordToBeUpdated);
                assetRawHistoryDao.flushCurrentChanges();
            }
        }
        return newRawRecords;
    }

    private AssetHistory getMostRecentHistoryRecordToUpdateAsset(AssetEntity asset,
                                                                 List<AssetHistory> incomingRecords) {
        AssetHistory mostRecentIncomingRecord = incomingRecords.get(0);
        Instant mostRecentIncomingEventDate = mostRecentIncomingRecord.getDateOfEvent().toInstant();

        List<AssetHistory> currentRecords = asset.getHistories();
        boolean recentIncomingIsMostRecent = true;
        for (AssetHistory record : currentRecords) {
            if (mostRecentIncomingEventDate.isBefore(record.getDateOfEvent().toInstant())) {
                recentIncomingIsMostRecent = false;
                break;
            }
        }
        if (recentIncomingIsMostRecent) {
            return mostRecentIncomingRecord;
        }
        return null;
    }

    private AssetEntity updateAssetFromMostRecentHistoryRecord(AssetEntity asset,
                                                               AssetHistory mostRecentRecord) {
        asset.setCFR(mostRecentRecord.getCfr());
        asset.setUvi(mostRecentRecord.getUvi());
        asset.setIRCS(mostRecentRecord.getIrcs());
        asset.setIccat(mostRecentRecord.getIccat());
        asset.setGfcm(mostRecentRecord.getGfcm());
        asset.setMMSI(mostRecentRecord.getMmsi());

        asset.setUpdatedBy(mostRecentRecord.getUpdatedBy());

        asset.setIMO(mostRecentRecord.getImo());
        asset.setIrcsIndicator(mostRecentRecord.getIrcsIndicator());
        //TODO is this correct?
        asset.setConstructionPlace(mostRecentRecord.getPortOfRegistration());
        asset.setHullMaterial(mostRecentRecord.getHullMaterial());
        Optional.ofNullable(mostRecentRecord.getConstructionDate())
                .ifPresent(date -> asset.setConstructionYear(Integer.toString(
                    LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC).getYear() + 1900)));
        Optional.ofNullable(mostRecentRecord.getCommissionDate()).ifPresent(date -> {
            LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
            asset.setCommissionDay(StringUtils.leftPad(Integer.toString(dateTime.getDayOfMonth()),2));
            asset.setCommissionMonth(StringUtils.leftPad(Integer.toString(dateTime.getMonthValue()), 2));
            asset.setCommissionYear(Integer.toString(dateTime.getYear()));
        });
        return asset;
    }

    private RawRecordStatus getCurrentRawRecordStatus(List<AssetHistory> records, AssetRawHistory rawRecord) {
        String targetHashKey = rawRecord.getHashKey();
        if (targetHashKey == null) {
            return new RawRecordStatus(false, false); //due to the lack of a hash key disregard the new raw record
        }
        for (AssetHistory record : records) {
            if (targetHashKey.equals( record.getHashKey() )) {
                boolean isUpdate = isCurrentRawRecordAnUpdate(record, rawRecord);
                return new RawRecordStatus(false, isUpdate, record);
            }
        }
        return new RawRecordStatus(true, false);
    }

    private boolean isCurrentRawRecordAnUpdate(AssetHistory duplicatedRecord, AssetRawHistory rawRecord) {
        Date dateOfEvent = rawRecord.getDateOfEvent();
        String ownerName = rawRecord.getOwnerName();
        String ownerAddress = rawRecord.getOwnerAddress();
        String agentAddress = rawRecord.getAgentAddress();
        String name = rawRecord.getName();
        String uvi = rawRecord.getUvi();
        String ircs = rawRecord.getIrcs();
        String externalMarking = rawRecord.getExternalMarking();
        String cfr = rawRecord.getCfr();
        String iccat = rawRecord.getIccat();
        BigDecimal loa = rawRecord.getLengthOverAll();
        BigDecimal lbp = rawRecord.getLengthBetweenPerpendiculars();
        BigDecimal powerOfMain = rawRecord.getPowerOfMainEngine();
        BigDecimal powerOfAux = rawRecord.getPowerOfAuxEngine();
        BigDecimal grossTonnage = rawRecord.getGrossTonnage();
        BigDecimal otherTonnage = rawRecord.getOtherTonnage();
        Boolean hasLicence = rawRecord.getHasLicence();
        String countryOfReg = rawRecord.getCountryOfRegistration();
        String placeOfReg = rawRecord.getPlaceOfRegistration();
        String countryOfImpOrExp = rawRecord.getCountryOfImportOrExport();
        String registrationNumber = rawRecord.getRegistrationNumber();
        String publicAid = rawRecord.getPublicAid();
        PublicAidEnum publicAidEnumVal = duplicatedRecord.getPublicAid();
        String mmsi = rawRecord.getMmsi();
        String imo = rawRecord.getImo();
        String typeOfExport = rawRecord.getTypeOfExport();
        TypeOfExportEnum typeOfExportEnumVal = duplicatedRecord.getTypeOfExport();
        String gfcm = rawRecord.getGfcm();
        String segment = rawRecord.getSegment();
        SegmentFUP segmentEnumVal = duplicatedRecord.getSegment();
        String eventCodeType = rawRecord.getEventCodeType();
        EventCodeEnum eventCodeEnumVal = duplicatedRecord.getEventCode();
        Date updateTime = rawRecord.getUpdateTime();
        BigDecimal safteyGrossTonnage = rawRecord.getSafteyGrossTonnage();
        String grossTonnageUnit = rawRecord.getGrossTonnageUnit();
        UnitTonnage grossTonnageUnitEnumVal = duplicatedRecord.getGrossTonnageUnit();
        String mainFishingGearType = rawRecord.getMainFishingGearType();
        FishingGear mainFishingGear = duplicatedRecord.getMainFishingGear();
        String subFishingGearType = rawRecord.getSubFishingGearType();
        FishingGear subFishingGear = duplicatedRecord.getSubFishingGear();

        boolean status =
                !(dateOfEvent == null || dateOfEvent.equals(duplicatedRecord.getDateOfEvent())) ||
                !(ownerName == null || ownerName.equals(duplicatedRecord.getOwnerName())) ||
                !(ownerAddress == null || ownerAddress.equals(duplicatedRecord.getOwnerAddress())) ||
                !(agentAddress == null || agentAddress.equals(duplicatedRecord.getAssetAgentAddress())) ||
                !(name == null || name.equals(duplicatedRecord.getName())) ||
                !(uvi == null || uvi.equals(duplicatedRecord.getUvi())) ||
                !(ircs == null || ircs.equals(duplicatedRecord.getIrcs())) ||
                !(externalMarking == null || externalMarking.equals(duplicatedRecord.getExternalMarking())) ||
                !(cfr == null || cfr.equals(duplicatedRecord.getCfr())) ||
                !(iccat == null || iccat.equals(duplicatedRecord.getIccat())) ||
                !(loa == null || loa.equals(duplicatedRecord.getLengthOverAll())) ||
                !(lbp == null || lbp.equals(duplicatedRecord.getLengthBetweenPerpendiculars())) ||
                !(powerOfMain == null || powerOfMain.equals(duplicatedRecord.getPowerOfMainEngine())) ||
                !(powerOfAux == null || powerOfAux.equals(duplicatedRecord.getPowerOfAuxEngine())) ||
                !(grossTonnage == null || grossTonnage.equals(duplicatedRecord.getGrossTonnage())) ||
                !(otherTonnage == null || otherTonnage.equals(duplicatedRecord.getOtherTonnage())) ||
                !(hasLicence == null || hasLicence.equals(duplicatedRecord.getHasLicence())) ||
                !(countryOfReg == null || countryOfReg.equals(duplicatedRecord.getCountryOfRegistration())) ||
                !(placeOfReg == null || placeOfReg.equals(duplicatedRecord.getPortOfRegistration())) ||
                !(countryOfImpOrExp == null ||
                        countryOfImpOrExp.equals(duplicatedRecord.getCountryOfImportOrExport())) ||
                !(registrationNumber == null ||
                        registrationNumber.equals(duplicatedRecord.getRegistrationNumber())) ||
                !(publicAid == null || publicAidEnumVal == null ||
                        publicAid.equals(publicAidEnumVal.name())) ||
                !(mmsi == null || mmsi.equals(duplicatedRecord.getMmsi())) ||
                !(imo == null || imo.equals(duplicatedRecord.getImo())) ||
                !(typeOfExport == null || typeOfExportEnumVal == null ||
                        typeOfExport.equals(typeOfExportEnumVal.name())) ||
                !(gfcm == null || gfcm.equals(duplicatedRecord.getGfcm())) ||
                !(segment == null || segmentEnumVal == null || segment.equals(segmentEnumVal.name())) ||
                !(eventCodeType == null || eventCodeEnumVal == null ||
                        eventCodeType.equals(eventCodeEnumVal.name())) ||
                !(updateTime == null || updateTime.equals(duplicatedRecord.getUpdateTime())) ||
                !(safteyGrossTonnage == null ||
                        safteyGrossTonnage.equals(duplicatedRecord.getSafteyGrossTonnage())) ||
                !(grossTonnageUnit == null || grossTonnageUnitEnumVal == null ||
                        grossTonnageUnit.equals(grossTonnageUnitEnumVal.name())) ||
                !(mainFishingGearType == null || mainFishingGear == null ||
                        mainFishingGearType.equals(mainFishingGear.getCode())) ||
                !(subFishingGearType == null || subFishingGear == null ||
                        subFishingGearType.equals(subFishingGear.getCode()));
        return status;
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    private final static class RawRecordStatus {
        private final boolean isNew;
        private final boolean isUpdate;
        private AssetHistory recordToBeUpdated = null;
    }
}
