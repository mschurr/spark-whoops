<#ftl strip_whitespace=true><#escape x as x?html>
<!DOCTYPE html>
<html>
<head>
    <title>Whoops! - ${basic_type}</title>
    <meta charset="utf-8"/>
    <style type="text/css">
      <#include "/public/whoops.css">
    </style>
</head>
<body>
<div class="Whoops container">
    <div class="stack-container">
        <#if has_frames>
        <div class="left-panel cf">
        <#else>
        <div class="left-panel cf empty">
        </#if>
        <header>
            <div class="exception">
                <div class="exc-title">
                    <#list 0..name?size-1 as i><#if i == name?size-1><span
                            class="exc-title-primary">${name[i]}</span><#else>${name[i]}.</#if></#list>

                <#--<?php if ($code): ?>
                  <span title="Exception Code">(<?php echo $tpl->escape($code) ?>)</span>
                <?php endif ?>-->
                </div>

                <span id="plain-exception">${plain_exception}</span>
                <button id="copy-button" class="clipboard" data-clipboard-text="${plain_exception}"
                        title="Copy exception details to clipabord">
                    COPY
                </button>

                <p class="exc-message">
                    <#if message != "">
                    ${message}
                    <#else>
                        <span class="exc-message-empty-notice">No message</span>
                    </#if>
                </p>
            </div>
        </header>

        <div class="frames-description">
            Stack frames (${frame_count}):
        </div>

        <div class="frames-container">
            <#list 0..frames?size-1 as i>
                <#if i == 0>
                <div class="frame active" id="frame-line-${i}">
                <#else>
                <div class="frame" id="frame-line-${i}">
                </#if>
                <div class="frame-method-info">
                    <span class="frame-index">${frame_count - i - 1}</span>
                    <span class="frame-class">${frames[i].class}</span>
                    <span class="frame-function">${frames[i].function}</span>
                </div>

                <span class="frame-file">
                  ${frames[i].file}<#if frames[i].file != "<#unknown>"></#if>
                      <#if frames[i].line != "-1">
                          <span class="frame-line">${frames[i].line}</span>
                      </#if>
                </span>
            </div>
            </#list>
        </div>
        </div>
        <div class="details-container cf">
            <!-- Frame Code -->
            <#if has_frames>
            <div class="frame-code-container">
            <#else>
            <div class="frame-code-container empty">
            </#if>
            <#list 0..frames?size-1 as i>
                <#assign frame = frames[i]>
                <#if i == 0>
                <div class="frame-code active" id="frame-code-${i}">
                <#else>
                <div class="frame-code" id="frame-code-${i}">
                </#if>
                <div class="frame-file">
                    <strong>${frames[i].file}</strong>
                    <#if frames[i].canonical_path??>
                        (${frames[i].canonical_path})
                    </#if>
                </div>
                <#if frames[i].code??>
                    <pre class="code-block prettyprint linenums:${frames[i].code_start}">${frames[i].code}</pre>
                </#if>

                <#if frames[i].comments?has_content>
                <div class="frame-comments">
                <#else>
                <div class="frame-comments empty">
                </#if>
            <#--<#list 0..frames[i].comments?size-1 as commentNo>
              <#assign comment = frames[i].comments[commentNo]>
              <div class="frame-comment" id="comment-${i}-${commentNo}">
                <span class="frame-comment-context">${comment.context}</span>
                ${comment.text}
              </div>
            </#list>-->
            </div>
            </div>
            </#list>
        </div>
            <!-- End Frame Code -->
            <!-- Details -->
            <div class="details">
                <h2 class="details-heading">Environment Details:</h2>

                <div class="data-table-container" id="data-tables">
                    <#list tables?keys as label>
                        <#assign data = tables[label]>
                        <div class="data-table">
                            <#if data?has_content>
                                <label>${label}</label>
                                <table class="data-table">
                                    <thead>
                                    <tr>
                                        <td class="data-table-k">Key</td>
                                        <td class="data-table-v">Value</td>
                                    </tr>
                                    </thead>
                                    <#list data?keys as k>
                                        <tr>
                                            <td>${k}</td>
                                            <td>${data[k]}</td>
                                        </tr>
                                    </#list>
                                </table>
                            <#else>
                                <label class="empty">${label}</label>
                                <span class="empty">EMPTY</span>
                            </#if>
                        </div>
                    </#list>
                </div>

                <!--<div class="data-table-container" id="handlers">
                  <label>Registered Handlers</label>
                    <div class="handler active">
                      1. WhoopsExceptionHandler
                    </div>
                </div>-->
            </div>
            <!-- End Details -->
        </div>
        </div>
        </div>
            <script><#include "/public/zepto.min.js"></script>
            <script><#include "/public/clipboard.min.js"></script>
            <script><#include "/public/prettify.min.js"></script>
            <script><#include "/public/whoops.base.js"></script>
</body>
</html>
</#escape>
