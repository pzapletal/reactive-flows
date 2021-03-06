/*
 * Copyright 2015 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.reactiveflows

import akka.actor.{ ActorDSL, ActorIdentity, ActorRef, Identify, Props }
import akka.testkit.{ EventFilter, TestProbe }

class ReactiveFlowsSpec extends BaseAkkaSpec {
  import ActorDSL._

  "Creating a ReactiveFlows actor" should {
    """result in logging "Up and running" at INFO level""" in {
      EventFilter.info(occurrences = 1, message = "Up and running").intercept {
        val mediator = TestProbe()
        val replicator = TestProbe()
        val flowShardRegion = TestProbe()
        system.actorOf(ReactiveFlows.props(mediator.ref, replicator.ref, flowShardRegion.ref))
      }
    }

    "result in creating a FlowFacade child actor" in {
      val sender = TestProbe()
      implicit val senderRef = sender.ref

      val mediator = TestProbe()
      val replicator = TestProbe()
      val flowShardRegion = TestProbe()
      val reactiveFlows = system.actorOf(ReactiveFlows.props(mediator.ref, replicator.ref, flowShardRegion.ref))
      sender.awaitAssert {
        system.actorSelection(reactiveFlows.path / FlowFacade.Name) ! Identify(None)
        sender.expectMsgPF() { case ActorIdentity(_, Some(_)) => () }
      }
    }
  }

  "ReactiveFlows" should {
    "terminate the system upon termination of a child actor" in {
      val probe = TestProbe()
      val mediator = TestProbe()
      val replicator = TestProbe()
      val flowShardRegion = TestProbe()
      actor(new ReactiveFlows(mediator.ref, replicator.ref, flowShardRegion.ref) {
        override protected def createFlowFacade() = actor(context)(new Act {
          context.stop(self)
        })
        override protected def onTerminated(actor: ActorRef) = probe.ref ! "terminated"
      })

      probe.expectMsg("terminated")
    }

    "terminate the system upon failure of a child actor" in {
      val probe = TestProbe()
      val mediator = TestProbe()
      val replicator = TestProbe()
      val flowShardRegion = TestProbe()
      actor(new ReactiveFlows(mediator.ref, replicator.ref, flowShardRegion.ref) {
        override protected def createFlowFacade() = actor(context)(new Act {
          self ! "blow-up"
          become {
            case "blow-up" => sys.error("Blown up!")
          }
        })
        override protected def onTerminated(actor: ActorRef) = probe.ref ! "terminated"
      })

      probe.expectMsg("terminated")
    }
  }
}
