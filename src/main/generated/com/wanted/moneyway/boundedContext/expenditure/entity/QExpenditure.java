package com.wanted.moneyway.boundedContext.expenditure.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExpenditure is a Querydsl query type for Expenditure
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExpenditure extends EntityPathBase<Expenditure> {

    private static final long serialVersionUID = 350800562L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExpenditure expenditure = new QExpenditure("expenditure");

    public final com.wanted.moneyway.boundedContext.category.entity.QCategory category;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isTotal = createBoolean("isTotal");

    public final com.wanted.moneyway.boundedContext.member.entity.QMember member;

    public final StringPath memo = createString("memo");

    public final DateTimePath<java.time.LocalDateTime> spendDate = createDateTime("spendDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> spendingPrice = createNumber("spendingPrice", Integer.class);

    public QExpenditure(String variable) {
        this(Expenditure.class, forVariable(variable), INITS);
    }

    public QExpenditure(Path<? extends Expenditure> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExpenditure(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExpenditure(PathMetadata metadata, PathInits inits) {
        this(Expenditure.class, metadata, inits);
    }

    public QExpenditure(Class<? extends Expenditure> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.wanted.moneyway.boundedContext.category.entity.QCategory(forProperty("category")) : null;
        this.member = inits.isInitialized("member") ? new com.wanted.moneyway.boundedContext.member.entity.QMember(forProperty("member")) : null;
    }

}

