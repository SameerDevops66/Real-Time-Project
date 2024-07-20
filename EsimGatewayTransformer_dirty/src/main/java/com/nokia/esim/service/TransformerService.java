package com.nokia.esim.service;

import com.nokia.esim.dto.ChangeEsimProfileStatusRequestDto;
import com.nokia.esim.dto.ChangeEsimProfileStatusResponseDto;
import com.nokia.esim.dto.GetEsimProfileStatusRequestDto;
import com.nokia.esim.dto.GetEsimProfileStatusResponseDto;
import com.nokia.esim.dto.PrepareEsimProfileRequestDto;
import com.nokia.esim.dto.PrepareEsimProfileResponseDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileRequestDto;
import com.nokia.esim.dto.UnlinkAndReserveProfileResponseDto;

public interface TransformerService
{

    public GetEsimProfileStatusResponseDto getEsimProfileStatus(
            GetEsimProfileStatusRequestDto esimProfileStatusRequestDto);

    public PrepareEsimProfileResponseDto prepareEsimProfile(PrepareEsimProfileRequestDto prepareEsimProfileRequestDto);

    public ChangeEsimProfileStatusResponseDto changeEsimProfileStatus(
            ChangeEsimProfileStatusRequestDto changeEsimProfileStatusRequestDto);

    public UnlinkAndReserveProfileResponseDto unlinkAndReserveProfile(
            UnlinkAndReserveProfileRequestDto unlinkAndReserveProfileRequestDto);

}
