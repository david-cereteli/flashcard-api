alter table cards
    add constraint UK_2v21n4svo7rkuo1e3q9nciel1 unique (front_content);

alter table users
    add constraint UK_6dotkott2kjsp8vw4d0m25fb7 unique (email);

alter table cards
    add constraint FKi7eg9pr1nooc66s02ht1h3ew8
        foreign key (deck_id)
            references decks(id);

alter table decks
    add constraint FKj0ey511pphfxbxbh8ri1616uv
        foreign key (user_id)
            references users(id);

alter table timings
    add constraint FKso2brfg8reusalfd2dhw2dkl8
        foreign key (card_id)
            references cards(id);