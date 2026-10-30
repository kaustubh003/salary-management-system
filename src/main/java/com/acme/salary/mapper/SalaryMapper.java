package com.acme.salary.mapper;

import com.acme.salary.dto.SalaryDto;
import com.acme.salary.entity.Salary;

public final class SalaryMapper {

    private SalaryMapper() {
    }

    public static SalaryDto toDto(Salary salary) {
        SalaryDto dto = new SalaryDto();
        dto.setId(salary.getId());
        dto.setEffectiveFrom(salary.getEffectiveFrom());
        dto.setEffectiveTill(salary.getEffectiveTill());
        dto.setCurrency(salary.getCurrency());
        dto.setBasicSalary(salary.getBasicSalary());
        dto.setHra(salary.getHra());
        dto.setDa(salary.getDa());
        dto.setTax(salary.getTax());
        dto.setEpf(salary.getEpf());
        dto.setGrossSalary(salary.getGrossSalary());
        dto.setNetSalary(salary.getNetSalary());
        return dto;
    }

    /**
     * Builds a new Salary. The entity constructor validates the currency, the amounts
     * and the date range, and recalculates the net. The id, gross and net in the DTO are ignored.
     */
    public static Salary toEntity(SalaryDto dto) {
        return new Salary(
                dto.getEffectiveFrom(),
                dto.getEffectiveTill(),
                dto.getCurrency(),
                dto.getBasicSalary(),
                dto.getHra(),
                dto.getDa(),
                dto.getTax(),
                dto.getEpf());
    }

    /**
     * Copies the editable fields onto an existing Salary. The id, gross and net in the DTO are ignored.
     */
    public static void updateEntity(SalaryDto dto, Salary salary) {
        // Clear the end date first. Setting the start and end one at a time would
        // otherwise fail the range check when both dates move to a new period.
        salary.setEffectiveTill(null);
        salary.setEffectiveFrom(dto.getEffectiveFrom());
        salary.setEffectiveTill(dto.getEffectiveTill());

        salary.setCurrency(dto.getCurrency());
        salary.setBasicSalary(dto.getBasicSalary());
        salary.setHra(dto.getHra());
        salary.setDa(dto.getDa());
        salary.setTax(dto.getTax());
        salary.setEpf(dto.getEpf());
    }
}