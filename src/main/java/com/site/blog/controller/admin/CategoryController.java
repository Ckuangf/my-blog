package com.site.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.site.blog.constants.BlogStatusConstants;
import com.site.blog.constants.HttpStatusConstants;
import com.site.blog.constants.SysConfigConstants;
import com.site.blog.dto.AjaxPutPage;
import com.site.blog.dto.AjaxResultPage;
import com.site.blog.dto.Result;
import com.site.blog.entity.BlogCategory;
import com.site.blog.entity.BlogInfo;
import com.site.blog.service.BlogCategoryService;
import com.site.blog.service.BlogInfoService;
import com.site.blog.util.DateUtils;
import com.site.blog.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @qq交流群 796794009
 * @qq 1320291471
 * @Description: 分类Controller
 * @date: 2019/8/6 17:24
 */
@Controller
@RequestMapping("/admin")
public class CategoryController {

    @Autowired
    private BlogCategoryService blogCategoryService;

    @Autowired
    private BlogInfoService blogInfoService;

    /**
     * 分类的集合数据[用于下拉框]
     *
     * @param
     * @return com.site.blog.dto.Result<com.site.blog.entity.BlogCategory>
     * @date 2019/8/30 14:38
     */
    @ResponseBody
    @GetMapping("/v1/category/list")
    public Result<BlogCategory> categoryList() {
        QueryWrapper<BlogCategory> queryWrapper = new QueryWrapper<BlogCategory>();
        queryWrapper.lambda().eq(BlogCategory::getIsDeleted, BlogStatusConstants.ZERO);
        List<BlogCategory> list = blogCategoryService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.OK, list);
    }

    @GetMapping("/v1/category")
    public String gotoBlogCategory() {
        return "adminLayui/category-list";
    }

    /**
     * 分类的分页
     *
     * @param ajaxPutPage
     * @param condition
     * @return com.site.blog.dto.AjaxResultPage<com.site.blog.entity.BlogCategory>
     * @date 2019/8/30 14:38
     */
    @ResponseBody
    @GetMapping("/v1/category/paging")
    public AjaxResultPage<BlogCategory> getCategoryList(AjaxPutPage<BlogCategory> ajaxPutPage, BlogCategory condition) {
        QueryWrapper<BlogCategory> queryWrapper = new QueryWrapper<>(condition);
        queryWrapper.lambda()
                .orderByAsc(BlogCategory::getCategoryRank)
                .ne(BlogCategory::getCategoryId, 1);
        Page<BlogCategory> page = ajaxPutPage.putPageToPage();
        blogCategoryService.page(page, queryWrapper);
        AjaxResultPage<BlogCategory> result = new AjaxResultPage<>();
        result.setData(page.getRecords());
        result.setCount(page.getTotal());
        return result;
    }

    /**
     * 修改分类信息
     *
     * @param blogCategory
     * @return com.site.blog.dto.Result
     * @date 2019/8/30 14:55
     */
    @ResponseBody
    @PostMapping("/v1/category/update")
    public Result updateCategory(BlogCategory blogCategory) {
        //todo 更新逻辑待修改
        BlogCategory sqlCategory = blogCategoryService.getById(blogCategory.getCategoryId());
        boolean flag = sqlCategory.getCategoryName().equals(blogCategory.getCategoryName());
        if (flag) {
            blogCategoryService.updateById(blogCategory);
        } else {
            BlogInfo blogInfo = new BlogInfo()
                    .setBlogCategoryId(blogCategory.getCategoryId())
                    .setBlogCategoryName(blogCategory.getCategoryName());
            UpdateWrapper<BlogInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(BlogInfo::getBlogCategoryId, blogCategory.getCategoryId());
            blogInfoService.update(blogInfo, updateWrapper);
            blogCategoryService.updateById(blogCategory);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
    }

    /**
     * 修改分类状态
     *
     * @param blogCategory
     * @return com.site.blog.dto.Result
     * @date 2019/8/30 14:55
     */
    @ResponseBody
    @PostMapping("/v1/category/isDel")
    public Result updateCategoryStatus(BlogCategory blogCategory) {
        boolean flag = blogCategoryService.updateById(blogCategory);
        if (flag) {
            return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
    }

    /**
     * 清除分类信息
     *
     * @param blogCategoryId
     * @return com.site.blog.dto.Result
     * @date 2019/9/1 15:48
     */
    @ResponseBody
    @PostMapping("/v1/category/clear")
    public Result clearCategory(Integer blogCategoryId) {
        boolean flag = clearCategoryById(blogCategoryId);
        if (flag) {
            return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @PostMapping("/v1/category/clearBatch")
    public Result batchClearCategory(String ids) {
        AtomicBoolean result = new AtomicBoolean(true);
        Arrays.stream(ids.split(",")).forEach(tagId -> {
            if (StringUtils.isNotEmpty(tagId)) {
                result.set(result.get() & clearCategoryById(Integer.valueOf(tagId)));
            }
        });
        if (result.get()) {
            return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
    }

    private boolean clearCategoryById(Integer blogCategoryId) {
        UpdateWrapper<BlogInfo> updateWrapper = new UpdateWrapper();
        updateWrapper.lambda()
                .eq(BlogInfo::getBlogCategoryId, blogCategoryId)
                .set(BlogInfo::getBlogCategoryId, SysConfigConstants.DEFAULT_CATEGORY.getConfigField())
                .set(BlogInfo::getBlogCategoryName, SysConfigConstants.DEFAULT_CATEGORY.getConfigName());
        boolean flag = blogInfoService.update(updateWrapper);
        flag = blogCategoryService.removeById(blogCategoryId);
        return flag;
    }

    @GetMapping("/v1/category/add")
    public String addBlogConfig() {
        return "adminLayui/category-add";
    }

    /**
     * 新增分类信息
     *
     * @param blogCategory
     * @return com.site.blog.dto.Result
     * @date 2019/9/1 15:48
     */
    @ResponseBody
    @PostMapping("/v1/category/add")
    public Result addCategory(BlogCategory blogCategory) {
        blogCategory.setCreateTime(DateUtils.getLocalCurrentDate());
        boolean flag = blogCategoryService.save(blogCategory);
        if (flag) {
            return ResultGenerator.getResultByHttp(HttpStatusConstants.OK);
        }
        return ResultGenerator.getResultByHttp(HttpStatusConstants.INTERNAL_SERVER_ERROR);
    }


}
