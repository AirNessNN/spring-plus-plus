package com.an.springplusplus.core.datasource.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/24 11:42 下午
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParam {

    private int index=1;

    private int size=10;
}
