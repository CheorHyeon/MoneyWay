package com.wanted.moneyway.boundedContext.plan.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlan is a Querydsl query type for Plan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlan extends EntityPathBase<Plan> {

    private static final long serialVersionUID = -1009301634L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPlan plan = new QPlan("plan");

    public final NumberPath<Integer> budget = createNumber("budget", Integer.class);

    public final com.wanted.moneyway.boundedContext.category.entity.QCategory category;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.wanted.moneyway.boundedContext.member.entity.QMember member;

    public QPlan(String variable) {
        this(Plan.class, forVariable(variable), INITS);
    }

    public QPlan(Path<? extends Plan> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPlan(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPlan(PathMetadata metadata, PathInits inits) {
        this(Plan.class, metadata, inits);
    }

    public QPlan(Class<? extends Plan> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.wanted.moneyway.boundedContext.category.entity.QCategory(forProperty("category")) : null;
        this.member = inits.isInitialized("member") ? new com.wanted.moneyway.boundedContext.member.entity.QMember(forProperty("member")) : null;
    }

}

