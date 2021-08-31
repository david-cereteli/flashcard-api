create table cards (
    id  bigserial not null,
    back_content TEXT not null,
    front_content VARCHAR(750) not null,
    deck_id int8,
    primary key (id)
);

create table decks (
    id  bigserial not null,
    name varchar(60),
    user_id int8,
    primary key (id)
);

create table timings (
     card_id int8 not null,
     easiness_factor float8 not null,
     last_review_date timestamp,
     repetition_interval int4 not null,
     repetition_number int4 not null,
     primary key (card_id)
);

create table users (
    id  bigserial not null,
    name varchar(128),
    email varchar(255) not null,
    primary key (id)
);