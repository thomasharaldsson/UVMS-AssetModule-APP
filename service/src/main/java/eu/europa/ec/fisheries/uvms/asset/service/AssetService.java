/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.asset.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import eu.europa.ec.fisheries.uvms.asset.exception.AssetServiceException;
import eu.europa.ec.fisheries.uvms.asset.message.AssetDataSourceQueue;
import eu.europa.ec.fisheries.uvms.asset.service.dto.AssetListResponse;
import eu.europa.ec.fisheries.uvms.asset.types.AssetId;
import eu.europa.ec.fisheries.uvms.constant.AssetIdentity;
import eu.europa.ec.fisheries.uvms.entity.Asset;
import eu.europa.ec.fisheries.uvms.entity.AssetGroup;
import eu.europa.ec.fisheries.uvms.entity.ContactInfo;
import eu.europa.ec.fisheries.uvms.entity.Note;
import eu.europa.ec.fisheries.uvms.mapper.SearchKeyValue;

public interface AssetService {

    /**
     * Create a new Asset
     *
     * @param asset
     * @return
     * @throws eu.europa.ec.fisheries.uvms.asset.exception.AssetServiceException
     */
    Asset createAsset(Asset asset, String username) throws AssetServiceException;

    /**
     * Get all Assets
     *
     * @param requestQuery
     * @return
     * @throws AssetServiceException
     */
    AssetListResponse getAssetList(List<SearchKeyValue> searchFields, int page, int listSize, boolean dynamic) throws AssetServiceException;

    /**
     * Get all Assets
     *
     * @param requestQuery
     * @return
     * @throws AssetServiceException
     */
    Long getAssetListCount(List<SearchKeyValue> searchFields, boolean dynamic) throws AssetServiceException;

    /**
     * Get a Asset by its asset id from the source queue
     *
     * @param assetId
     * @param source
     * @return
     * @throws AssetServiceException
     */
    Asset getAssetById(AssetIdentity assetId, String value) throws AssetServiceException;

    /**
     * Get Asset By internal Id
     *
     * @param id
     * @return
     * @throws AssetServiceException
     */
    Asset getAssetById(UUID id) throws AssetServiceException;

    /**
     * Update a Asset
     *
     * @param asset
     * @param username
     * @param comment
     * @return
     * @throws AssetServiceException
     */
    Asset updateAsset(Asset asset, String username, String comment) throws AssetServiceException;

    /**
     * Archives an asset.
     *
     * @param asset   an asset
     * @param comment a comment to the archiving
     * @return the archived asset
     * @throws AssetServiceException if unsuccessful
     */
    Asset archiveAsset(Asset asset, String username, String comment) throws AssetServiceException;

    /**
     * Create asset if not exists, otherwise update asset
     *
     * @param asset
     * @return
     * @throws AssetServiceException
     */
    Asset upsertAsset(Asset asset, String username) throws AssetServiceException;

    /**
     * Returns a list of assets based on the searh criterias in the
     * assetgroups
     *
     * @param groups
     * @return
     * @throws AssetServiceException
     */
    List<Asset> getAssetListByAssetGroups(List<AssetGroup> groups) throws AssetServiceException;


        //AssetListGroupByFlagStateResponse getAssetListGroupByFlagState(List assetIds) throws AssetServiceException;
    Object getAssetListGroupByFlagState(List assetIds) throws AssetServiceException;

    void deleteAsset(AssetIdentity assetId, String value) throws AssetServiceException;


    /**
     * return all revisions for an asset
     *
     * @param asset
     * @return
     * @throws AssetServiceException
     */
    List<Asset> getRevisionsForAsset(Asset asset) throws AssetServiceException;


    /**
     * return asset for specific historyId
     *
     * @param asset
     * @param historyId
     * @return
     * @throws AssetServiceException
     */
    Asset getAssetRevisionForRevisionId(UUID historyId) throws AssetServiceException;


    /** return asset as it was specidied date
     *
     * @param idType
     * @param idValue
     * @param date
     * @return
     * @throws AssetServiceException
     */
    Asset getAssetFromAssetIdAtDate(AssetIdentity idType, String idValue, LocalDateTime date) throws AssetServiceException;
    
    /**
     * Returns all notes for given asset UUID.
     * 
     * @param assetId
     * @return
     */
    List<Note> getNotesForAsset(UUID assetId);
    

    /**
     * Create a note for given asset UUID.
     * 
     * @param assetId
     * @param note
     * @return
     */
    Note createNoteForAsset(UUID assetId, Note note, String username);
    
    /**
     * Update a note.
     * 
     * @param note
     * @return
     */
    Note updateNote(Note note, String username);
    
    /**
     * Delete a note with given id
     * 
     * @param id
     */
    void deleteNote(UUID id);
    
    /**
     * Returns all contact info for given asset UUID.
     * 
     * @param assetId
     * @return
     */
    List<ContactInfo> getContactInfoForAsset(UUID assetId);
    

    /**
     * Create a contact info for given asset UUID.
     * 
     * @param assetId
     * @param note
     * @return
     */
    ContactInfo createContactInfoForAsset(UUID assetId, ContactInfo contactInfo, String username);
    
    /**
     * Update a contact info.
     * 
     * @param note
     * @return
     */
    ContactInfo updateContactInfo(ContactInfo contactInfo, String username);
    
    /**
     * Delete the contact info with given id
     * 
     * @param id
     */
    void deleteContactInfo(UUID id);
}

