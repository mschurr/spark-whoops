<#escape x as x?html>
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
        <div class="left-panel cf <#if !frames?has_content>empty</#if>">
            <header>
                <div class="exception">
                    <div class="exc-title">
                        <#list name as namePart><span>${namePart}</span></#list>
                    </div>
                    <span id="plain-exception">${plain_exception}</span>
                    <button id="copy-button" class="clipboard" data-clipboard-text="${plain_exception}" title="Copy exception details to clipabord">
                        Copy stacktrace
                    </button>
                    <p class="exc-message">
                        <#if message == "">No Exception Message<#else>${message}</#if>
                    </p>
                </div>
            </header>
            <div class="frames-description">Stack frames (${frames?size}):</div>
            <div class="frames-container">
                <#list frames as frame>
                    <div class="frame <#if frame?index == 0>active</#if>" id="frame-line-${frame?index}">
                        <div class="frame-method-info">
                            <span class="frame-index">${frames?size - frame?index - 1}</span>
                            <span class="frame-class">${frame.class}</span>
                            <span class="frame-function">${frame.function}</span>
                        </div>
                        <span class="frame-file">${frame.file}<#if frame.line != "-1"><span class="frame-line">${frame.line}</span></#if></span>
                    </div>
                </#list>
            </div>
        </div>
        <div class="details-container cf">
            <div class="frame-code-container <#if !frames?has_content>empty</#if>">
                <#list frames as frame>
                    <div class="frame-code <#if frame?index == 0>active</#if>" id="frame-code-${frame?index}">
                        <div class="frame-file">
                            <strong>${frame.file}</strong>
                            <#if frame.canonical_path??>
                                (${frame.canonical_path})
                            </#if>
                        </div>
                        <#if frame.code??>
                            <pre class="code-block prettyprint linenums:${frame.code_start}">${frame.code}</pre>
                        </#if>
                        <div class="frame-comments <#if !frame.comments?has_content>empty</#if>">
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
