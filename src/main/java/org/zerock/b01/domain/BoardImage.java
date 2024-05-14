package org.zerock.b01.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@ToString(exclude = "board")  게시판 클래스 생성 시 주석 해제 후 수정
public class BoardImage implements Comparable<BoardImage> {

    @Id
    private String uuid;

    private String fileName;

    private int ord;

//    @ManyToOne
//    private Board board;  // 게시판 객체


    @Override
    public int compareTo(BoardImage other) {
        return this.ord - other.ord;
    }

//    public void changeBoard(Board board) { 게시판 클래스 생성 시 주석해제
//        this.board = board
//    }
}
