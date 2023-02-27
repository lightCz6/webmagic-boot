package com.bidizhaobiao.data.Crawl.service.impl.DX010951;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 日期: 2023/2/27
 * 原网站: https://xaztgdjt.com/news-5.html
 * 主页: https://xaztgdjt.com/
 */
@Service
public class DX010951_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "https://xaztgdjt.com/news-5.html";
    public String baseUrl = "https://xaztgdjt.com";
    // 网站编号
    public String sourceNum = "DX010951";
    // 网站名称
    public String sourceName = "西安中铁轨道交通有限公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "西北";
    // 设置省份
    public String province = "陕西";
    // 设置城市
    public String city = "西安";
    // 设置县
    public String district = "";
    // 设置CreateBy
    public String createBy = "何康";

    public boolean isNeedSaveFileAddSSL = true;

    public Pattern datePat = Pattern.compile("(\\d{4})(\\.|年|/|-)(\\d{1,2})(\\.|月|/|-)(\\d{1,2})");

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);

    public Site getSite() {
        return this.site
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36")
                .setTimeOut(3000)
                .setRetryTimes(3);
    }

    @Override
    public void startCrawl(int threadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
        // 保存日志
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(threadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        Request request = new Request(listUrl);
        spider.addRequest(request);
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        String html = page.getHtml().toString();
        try {
            if (url.contains("page")) {
                Document listDoc = Jsoup.parse(html);
                Elements liList = listDoc.select("li.fnw");
                ArrayList<BranchNew> branchNews = new ArrayList<>();
                if (liList.size() > 0) {
                    for (Element li : liList) {
                        Element a = li.select("a").first();
                        String link = a.attr("href");;
                        String id = link.substring(1);
                        String listTitle = a.text();
                        String detailLink = baseUrl + link;
                        String listDate = li.select("div.years").text() + li.select("div.days");
                        if (!CheckProclamationUtil.isProclamationValuable(listTitle)) {
                            continue;
                        }
                        BranchNew branchNew = new BranchNew();
                        branchNew.setId(id);
                        serviceContext.setCurrentRecord(id);
                        branchNew.setTitle(listTitle);
                        branchNew.setDetailLink(detailLink);
                        branchNew.setDate(listDate);
                        branchNews.add(branchNew);
                    }

                    List<BranchNew> branchNewList = checkData(branchNews, serviceContext);
                    for (BranchNew branchNew : branchNewList) {
                        map.put(branchNew.getDetailLink(),branchNew);
                        page.addTargetRequest(branchNew.getDetailLink());
                    }

                    Elements aList = listDoc.select("ul.pagination a");
                    for (Element a : aList) {
                        String text = a.text();
                        String next = a.attr("href");
                        if (text.equals("下一页") && next != null) {
                            String nextPage = baseUrl + next;
                            page.addTargetRequest(nextPage);
                            break;
                        }
                    }

                } else {
                    dealWithNullListPage(serviceContext);
                }



            }else{
                BranchNew branch = map.get(url);
                if(branch != null){
                    Document detailDoc = Jsoup.parse(html);
                    Element mainContent = detailDoc.select("div.l").first();
                    String detailTitle = null;
                    String content = null;
                    if(mainContent != null){
                         detailTitle = mainContent.select("div.showhead").first().text();
                        Elements aList = mainContent.select("a");
                        for (Element a : aList) {
                            String href = a.attr("href").replace(" ", "%20");
                            if (!href.startsWith("/") && !href.startsWith("http")) {
                                href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                a.attr("href", href);
                            }
                            a.attr("rel", "noreferrer");
                            if (href.startsWith("mailto")) {
                                continue;
                            }
                            if (href.contains("javascript") || href.equals("#")) {
                                if (a.attr("onclick").contains("window.open('http")) {
                                    String onclick = a.attr("onclick");
                                    onclick = onclick.substring(onclick.indexOf("'") + 1, onclick.lastIndexOf("'"));
                                    a.attr("href", onclick);
                                    a.removeAttr("onclick");
                                } else {
                                    a.removeAttr("href");
                                    a.removeAttr("onclick");
                                }
                                continue;
                            }
                            if (href.length() > 10 && href.indexOf("http") != 0) {
                                if (href.indexOf("//www") == 0) {
                                    href = baseUrl.substring(0, baseUrl.indexOf(":") + 1) + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("../") == 0) {
                                    href = href.replace("../", "");
                                    href = baseUrl + "/" + href;
                                    a.attr("href", href);
                                } else if (href.startsWith("/")) {
                                    href = baseUrl + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("./") == 0) {
                                    href = url.substring(0, url.lastIndexOf("/")) + href.substring(href.lastIndexOf("./") + 1);
                                    a.attr("href", href);
                                } else {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                    a.attr("href", href);
                                }
                            }
                        }
                        Elements imgList = mainContent.select("img");
                        for (Element img : imgList) {
                            String src = img.attr("src").replace(" ", "%20");
                            if (src.startsWith("file://")) {
                                img.remove();
                                continue;
                            }
                            if (src.contains("data:image")) {
                                continue;
                            }
                            if (src.length() > 10 && src.indexOf("http") != 0) {
                                if (src.indexOf("../") == 0) {
                                    src = src.replace("../", "");
                                    src = baseUrl + "/" + src;
                                    img.attr("src", src);
                                } else if (src.indexOf("./") == 0) {
                                    src = url.substring(0, url.lastIndexOf("/")) + src.substring(src.lastIndexOf("./") + 1);
                                    img.attr("src", src);
                                } else if (src.startsWith("//")) {
                                    src = baseUrl.substring(0, baseUrl.indexOf(":") + 1) + src;
                                    img.attr("src", src);
                                } else if (src.indexOf("/") == 0) {
                                    src = baseUrl + src;
                                    img.attr("src", src);
                                } else {
                                    src = url.substring(0, url.lastIndexOf("/") + 1) + src;
                                    img.attr("src", src);
                                }
                            }
                        }
                        mainContent.select("input").remove();
                        mainContent.select("meta").remove();
                        mainContent.select("script").remove();
                        mainContent.select("link").remove();
                        mainContent.select("style").remove();
                        content = mainContent.toString();
                    }
                    if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + detailTitle + "</a></div>";
                        html = Jsoup.parse(content).toString();
                    }

                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(branch.getId());
                    recordVO.setDdid(SpecialUtil.stringMd5(html));
                    recordVO.setListTitle(branch.getTitle());
                    recordVO.setDate(branch.getDate());
                    recordVO.setTitle(detailTitle);
                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(html);
                    recordVO.setContent(content);
                    dataStorage(serviceContext, recordVO, branch.getType());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
