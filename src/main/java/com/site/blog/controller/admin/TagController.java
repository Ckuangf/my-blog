package com.site.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.site.blog.constants.BlogStatusConstants;
import com.site.blog.constants.HttpStatusConstants;
import com.site.blog.constants.SysConfigConstants;
import com.site.blog.dto.AjaxPutPage;
import com.site.blog.dto.AjaxResultPage;
import com.site.blog.dto.Result;
import com.site.blog.entity.BlogInfo;
import com.site.blog.entity.BlogTag;
import com.site.blog.entity.BlogTagRelation;
import com.site.blog.service.BlogInfoService;
import com.site.blog.service.BlogTagRelationService;
import com.site.blog.service.BlogTagService;
import com.site.blog.util.DateUtils;
import com.site.blog.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 标签Controller
 * @date: 2019/8/6 17:24
 */
@Controller
@RequestMapping("/admin")
public class TagController {

    @Autowired
    private BlogTagService blogTagService;

    @Autowired
    private BlogInfoService blogInfoService;

    @Autowired
    private BlogTagRelationService blogTagRelationService;


    @GetMapping("/v1/tags")
    public String gotoTag() {
        return "adminLayui/tag-list";
    }

    /**
     * @Description: 返回启用状态下的所有标签
     * @Param: []
     * @date: 2019/8/26 10:13
     */
    @ResponseBody
    @GetMapping("/v1/tags/list")
    public Result<BlogTag> tagsList() {
        QueryWrapper<BlogTag> queryWrapper = new QueryWrapper<BlogTag>();
        queryWrapper.lambda().eq(BlogTag::getIsDeleted, BlogStatusConstants.ZERO);
        List<BlogTag> list = blogTagService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.OK, list);
    }

    /**
     * 标签分页
     *
     * @param ajaxPutPage
     * @param condition
     * @return com.site.blog.dto.AjaxResultPage<com.site.blog.entity.BlogTag>
     * @date 2019/9/1 11:20
     */
    @ResponseBody
    @GetMapping("/v1/tags/paging")
    public AjaxResultPage<BlogTag> getTagsList(AjaxPutPage<BlogTag> ajaxPutPage, BlogTag condition) {
        QueryWrapper<BlogTag> queryWrapper = new QueryWrapper<>(condition);
        queryWrapper.lambda()
                .ne(BlogTag::getTagId, 1);
        Page<BlogTag> page = ajaxPutPage.putPageToPage();
        blogTagService.page(page, queryWrapper);
        AjaxResultPage<BlogTag> result = new AjaxResultPage<>();
        result.setData(page.getRecords());
        result.setCount(page.getTotal());
        return result;
    }

    /**
     * 修改标签
     *
     * @return com.site.blog.dto.AjaxResultPage<com.site.blog.entity.BlogTag>
     * @date 2019/9/1 11:20
     */
    @ResponseBody
    @PostMapping("/v1/tags/update")
    public Result updateTagInfo(BlogTag blogTag) {
        //todo 添加参数校验  spring 的validate校验抛出 notBlank的异常 待确认
        BlogTag byId = blogTagService.getById(blogTag.getTagId());
        if (null == byId) {
            return ResultGenerator.genFailResult("未找到待修改的标签");
        }
        UpdateWrapper<BlogTag> updateWrapper = new UpdateWrapper<>();
        byId.setTagName(blogTag.getTagName());
        updateWrapper.eq("tag_id",blogTag.getTagId());
        boolean update = blogTagService.update(byId, updateWrapper);
        if(update){
            return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
        }else{
            return ResultGenerator.genFailResult("更新标签: "+"blogTag.getTagId()"+"失败");
        }
    }


    /**
     * 修改标签状态
     *
     * @param blogTag
     * @return com.site.blog.dto.Result
     * @date 2019/8/30 14:55
     */
    @ResponseBody
    @PostMapping("/v1/tags/isDel")
    public Result updateCategoryStatus(BlogTag blogTag) {
        boolean flag = blogTagService.updateById(blogTag);
        if (flag) {
            return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
    }

    /**
     * 添加标签
     *
     * @param blogTag
     * @return com.site.blog.dto.Result
     * @date 2019/9/2 10:12
     */
    @ResponseBody
    @PostMapping("/v1/tags/add")
    public Result addTag(BlogTag blogTag) {
        blogTag.setCreateTime(DateUtils.getLocalCurrentDate());
        boolean flag = blogTagService.save(blogTag);
        if (flag) {
            return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
        } else {
            return ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 清除标签
     *
     * @param tagId
     * @return com.site.blog.dto.Result
     * @date 2019/9/2 18:41
     */
    @ResponseBody
    @PostMapping("/v1/tags/clear")
    public Result clearTag(Integer tagId) throws RuntimeException {
        clearTagById(tagId);
        return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
    }

    private void clearTagById(Integer tagId) {
        QueryWrapper<BlogTagRelation> blogTagRelationQuery = new QueryWrapper<>();
        blogTagRelationQuery.lambda().eq(BlogTagRelation::getTagId, tagId);
        List<BlogTagRelation> blogTagRelationList = blogTagRelationService.list(blogTagRelationQuery);
        if (blogTagRelationList.size() > 0) {
            //更新blogInfo中的tags信息
            List<BlogInfo> collect = blogTagRelationList.stream().map(blogTagRelation -> {
                BlogInfo blogInfo = blogInfoService.getById(blogTagRelation.getBlogId());
                String blogTags = blogInfo.getBlogTags();
                BlogTag blogTag = blogTagService.getById(blogTagRelation.getTagId());
                List<String> stringList = Arrays.stream(blogTags.split(",")).map(tagName -> {
                    if (tagName.equalsIgnoreCase(blogTag.getTagName())) {
                        return SysConfigConstants.DEFAULT_TAG.getConfigName();
                    } else {
                        return tagName;
                    }
                }).collect(Collectors.toList());
                blogInfo.setBlogTags(StringUtils.strip(stringList.toString(), "[]"));
                return blogInfo;
            }).collect(Collectors.toList());
            blogInfoService.updateBatchById(collect);

            //更新blogTagRelation中的信息
            List<BlogTagRelation> blogTagRelationCollection = blogTagRelationList.stream().map(blogTagRelation -> {
                if (blogTagRelation.getTagId() == tagId) {
                    blogTagRelation.setTagId(Integer.valueOf(SysConfigConstants.DEFAULT_TAG.getConfigField()));
                    return blogTagRelation;
                } else {
                    return blogTagRelation;
                }
            }).collect(Collectors.toList());
            blogTagRelationService.updateBatchById(blogTagRelationCollection);
        }

        /*QueryWrapper<BlogTagRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BlogTagRelation::getTagId, tagId);
        List<BlogTagRelation> tagRelationList = blogTagRelationService.list(queryWrapper);
        // 批量更新的BlogInfo信息
        List<BlogInfo> infoList = tagRelationList.stream()
                .map(tagRelation -> new BlogInfo()
                        .setBlogId(tagRelation.getBlogId())
                        .setBlogTags(SysConfigConstants.DEFAULT_TAG.getConfigName())).collect(Collectors.toList());
        List<Long> blogIds = infoList.stream().map(BlogInfo::getBlogId).collect(Collectors.toList());
        // 批量更新的tagRelation信息
        List<BlogTagRelation> tagRelations = tagRelationList.stream()
                .map(tagRelation -> new BlogTagRelation()
                        .setBlogId(tagRelation.getBlogId())
                        .setTagId(Integer.valueOf(SysConfigConstants.DEFAULT_CATEGORY.getConfigField())))
                .collect(Collectors.toList());
        blogInfoService.updateBatchById(infoList);
        blogTagRelationService.remove(new QueryWrapper<BlogTagRelation>()
                .lambda()
                .in(BlogTagRelation::getBlogId, blogIds));
        blogTagRelationService.saveBatch(tagRelations);*/
        blogTagService.removeById(tagId);
    }

    @ResponseBody
    @PostMapping("/v1/tags/clearBatch")
    public Result batchClearTag(String tagIds) throws RuntimeException {
        Arrays.stream(tagIds.split(",")).forEach(tagId -> {
            if (StringUtils.isNotEmpty(tagId)) {
                clearTagById(Integer.valueOf(tagId));
            }
        });
        return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
    }
}
