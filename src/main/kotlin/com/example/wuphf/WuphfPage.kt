package com.example.wuphf

import org.springframework.stereotype.Component
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context

@Component
class WuphfPage(private val templates: ITemplateEngine) {

    fun render(report: VisitReport): String =
        templates.process(TEMPLATE, Context().apply { setVariable("report", report) })

    companion object {
        const val TEMPLATE = "wuphf"
    }
}
