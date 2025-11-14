package com.capstone.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyOption extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "survey_question_id")
    @JsonIgnore
    private SurveyQuestion  surveyQuestion;

    private String optionContent;

    @OneToMany(mappedBy = "surveyOption")
    private List<SurveyQuetionSelected> surveyQuestionSelected;
}
