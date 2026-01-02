#
# Copyright (C) 2022 FlamingoOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Dolby path
DOLBY_PATH := hardware/dolby

# Soong Namespace
PRODUCT_SOONG_NAMESPACES += \
   $(DOLBY_PATH)

# Enable codec support
AUDIO_FEATURE_ENABLED_DS2_DOLBY_DAP := true

# SEPolicy
BOARD_VENDOR_SEPOLICY_DIRS += $(DOLBY_PATH)/sepolicy/vendor

# HIDL
DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE += $(DOLBY_PATH)/dolby_framework_matrix.xml
DEVICE_MANIFEST_FILE += $(DOLBY_PATH)/vendor.dolby.hardware.dms@2.0-service.xml
DEVICE_MANIFEST_FILE += $(DOLBY_PATH)/vendor.dolby.media.c2.xml
    
# Configs
PRODUCT_COPY_FILES += \
    $(DOLBY_PATH)/configs/dax-default.xml:$(TARGET_COPY_OUT_VENDOR)/etc/dolby/dax-default.xml \
    $(DOLBY_PATH)/configs/media_codecs_dolby_audio.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_dolby_audio.xml

# Dolby VNDK libs
PRODUCT_PACKAGES += \
    libstagefright_foundation-v33

PRODUCT_PACKAGES += \
    libshim_dolby

# Init
PRODUCT_PACKAGES += \
    init.dolby.rc

# Overlays    
PRODUCT_PACKAGES += \
    DolbyFrameworksResCommon

# Spatial Audio
PRODUCT_COPY_FILES += \
    $(DOLBY_PATH)/configs/android.hardware.sensor.dynamic.head_tracker.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.dynamic.head_tracker.xml \

# Spatial Audio: optimize spatializer effect
PRODUCT_PROPERTY_OVERRIDES += \
       audio.spatializer.effect.util_clamp_min=300

# Spatial Audio: declare use of spatial audio
PRODUCT_PROPERTY_OVERRIDES += \
       ro.audio.spatializer_enabled=true \
       ro.audio.headtracking_enabled=true \
       ro.audio.spatializer_transaural_enabled_default=false \
       persist.vendor.audio.spatializer.speaker_enabled=true \

# Spatial Audio Proprietary blobs
PRODUCT_PACKAGES += \
    libspatialaudio

# Media (C2)
PRODUCT_PACKAGES += \
    android.hardware.media.c2@1.0.vendor \
    android.hardware.media.c2@1.1.vendor \
    android.hardware.media.c2@1.2.vendor \
    libcodec2_hidl@1.2.vendor \
    libsfplugin_ccodec_utils.vendor \
    libcodec2_soft_common.vendor

# Codec2 Props
PRODUCT_VENDOR_PROPERTIES += \
    vendor.audio.c2.preferred=true \
    debug.c2.use_dmabufheaps=1 \
    vendor.qc2audio.suspend.enabled=true \
    vendor.qc2audio.per_frame.flac.dec.enabled=true

# Dolby Props
PRODUCT_VENDOR_PROPERTIES += \
    ro.vendor.dolby.dax.version=DAX3_3.7.0.8_r1 \
    vendor.audio.dolby.ds2.hardbypass=false \
    vendor.audio.dolby.ds2.enabled=false

# Remove Packages for Dolby Support
PRODUCT_PACKAGES += \
    RemovePackagesDolby

# DSP Volume Synchronizer
PRODUCT_PACKAGES += \
    DSPVolumeSynchronizer

# DolbyManager
PRODUCT_PACKAGES += \
    DolbyManager

# Dolby Proprietary blobs
PRODUCT_COPY_FILES += \
    $(DOLBY_PATH)/proprietary/vendor/etc/init/vendor.dolby.hardware.dms@2.0-service.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/vendor.dolby.hardware.dms@2.0-service.rc \

# Codec2 (Dolby)
PRODUCT_COPY_FILES += \
    $(DOLBY_PATH)/proprietary/vendor/etc/init/vendor.dolby.media.c2@1.0-service.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/vendor.dolby.media.c2@1.0-service.rc \

PRODUCT_PACKAGES += \
    libdapparamstorage \
    libdlbpreg \
    vendor.dolby.hardware.dms@2.0 \
    libdlbdsservice \
    liboem_specific \
    vendor.dolby.hardware.dms@2.0-impl \
    vendor.dolby.hardware.dms@2.0-service \
    libcodec2_soft_ac4dec \
    libcodec2_soft_ddpdec \
    libcodec2_soft_dolby \
    libcodec2_store_dolby \
    libdeccfg \
    vendor.dolby.media.c2@1.0-service \
    libdlbvol \
    libswdap \
    libswgamedap \
    libswvqe \
