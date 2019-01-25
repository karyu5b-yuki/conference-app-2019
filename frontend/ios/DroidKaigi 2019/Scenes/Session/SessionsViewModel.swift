//
//  SessionsViewModel.swift
//  DroidKaigi 2019
//
//  Created by Akira Iwaya on 2019/01/14.
//

import Foundation
import ios_combined
import RxSwift
import RxCocoa

final class SessionsViewModel {

    private let repository =  SessionRepository()
    private let bag = DisposeBag()
    init() {}
    private let _error = BehaviorRelay<String?>(value: nil)
}

extension SessionsViewModel {

    struct Input {
        let initTrigger: Observable<Void>
    }

    struct Output {
        let sessions: Driver<[SessionByStartTime]>
        let error: Driver<String?>
    }
    
    func transform(input: Input) -> Output {
        let sessionContents = input.initTrigger
                .flatMap { [weak self] (_) -> Observable<SessionContents> in
                    guard let `self` = self else { return Observable.empty() }
                    return self.repository.fetch()
                        .asObservable()
                        .catchError { error in
                            self._error.accept(error.localizedDescription)
                            return Observable.empty()
                        }
                }
        
        let dateFormatter = DateFormatter()
        dateFormatter.timeZone = TimeZone(identifier: "Asia/Tokyo")
        dateFormatter.locale = NSLocale.current
        dateFormatter.dateFormat = "HH:mm"
        
        let sessions = sessionContents.map { sessionContents -> [SessionByStartTime] in
            return sessionContents.sessions.reduce(into: [SessionByStartTime]()) { result, session in
                if let index = result.firstIndex(where: { $0.startTime == session.startTime }) {
                    result[index].sessions.append(session)
                    return
                }
                result.append(SessionByStartTime(startDayText: session.startDayText,
                                                 startTimeText: dateFormatter.string(from: Date(timeIntervalSince1970: session.startTime / 1000)),
                                                 startTime: session.startTime,
                                                 sessions: [session]))
            }
        }.asDriver(onErrorJustReturn: [])
        let error = _error.asDriver()
        return Output(sessions: sessions, error: error)
    }
}
